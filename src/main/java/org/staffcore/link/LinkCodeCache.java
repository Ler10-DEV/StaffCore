package org.staffcore.link;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkCodeCache {
    private final Map<String, PendingCode> codeMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerMap = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final int ttlSeconds;
    private final String prefix;

    public record PendingCode(UUID uuid, String code, long expiresAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public LinkCodeCache(int ttlSeconds, String prefix) {
        this.ttlSeconds = ttlSeconds > 0 ? ttlSeconds : 300;
        this.prefix = prefix != null ? prefix : "MC-";
    }

    public synchronized String generateCode(UUID uuid) {
        
        String existingCode = playerMap.remove(uuid);
        if (existingCode != null) {
            codeMap.remove(existingCode);
        }

        String code = null;
        for (int i = 0; i < 10; i++) {
            int num = random.nextInt(9000) + 1000;
            String candidate = prefix + num;
            if (!codeMap.containsKey(candidate)) {
                code = candidate;
                break;
            }
        }

        if (code == null) {
            code = prefix + (System.currentTimeMillis() % 10000);
        }

        long expiresAt = System.currentTimeMillis() + (ttlSeconds * 1000L);
        PendingCode pending = new PendingCode(uuid, code, expiresAt);
        codeMap.put(code, pending);
        playerMap.put(uuid, code);
        return code;
    }

    public synchronized Optional<UUID> consumeCode(String code) {
        if (code == null) return Optional.empty();
        String normalized = code.trim().toUpperCase();
        PendingCode pending = codeMap.remove(normalized);
        if (pending == null && !normalized.startsWith(prefix.toUpperCase())) {
            pending = codeMap.remove((prefix.toUpperCase() + normalized));
        }
        if (pending == null) {
            return Optional.empty();
        }
        playerMap.remove(pending.uuid());
        if (pending.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(pending.uuid());
    }

    public synchronized void cleanupExpired() {
        long now = System.currentTimeMillis();
        codeMap.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                playerMap.remove(entry.getValue().uuid());
                return true;
            }
            return false;
        });
    }

    public Optional<PendingCode> getPending(UUID uuid) {
        String code = playerMap.get(uuid);
        if (code == null) return Optional.empty();
        PendingCode pending = codeMap.get(code);
        if (pending == null || pending.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(pending);
    }
}
