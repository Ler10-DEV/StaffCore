package org.staffcore.storage.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.staffcore.storage.dao.CommandLogDAO;
import org.staffcore.storage.model.CommandEntry;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JsonCommandLogDAO implements CommandLogDAO {
    private final Path logsDir;
    private final Gson gson;
    private final Logger logger;
    private final int maxBufferSize;
    private final Map<UUID, Deque<CommandEntry>> playerBuffers = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();

    public JsonCommandLogDAO(Path logsDir, Gson gson, Logger logger, int maxBufferSize) {
        this.logsDir = logsDir;
        this.gson = gson;
        this.logger = logger;
        this.maxBufferSize = maxBufferSize > 0 ? maxBufferSize : 54;
    }

    private Deque<CommandEntry> getOrCreateBuffer(UUID playerUuid) {
        return playerBuffers.computeIfAbsent(playerUuid, uuid -> {
            Deque<CommandEntry> deque = new ConcurrentLinkedDeque<>();
            Path playerFile = logsDir.resolve(uuid.toString() + ".json");
            if (Files.exists(playerFile)) {
                try (Reader reader = Files.newBufferedReader(playerFile, StandardCharsets.UTF_8)) {
                    Type listType = new TypeToken<List<CommandEntry>>(){}.getType();
                    List<CommandEntry> list = gson.fromJson(reader, listType);
                    if (list != null) {
                        deque.addAll(list);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to read command logs for " + uuid + ": " + e.getMessage());
                }
            }
            return deque;
        });
    }

    @Override
    public void logCommand(UUID playerUuid, CommandEntry entry) {
        if (playerUuid == null || entry == null) return;
        Deque<CommandEntry> deque = getOrCreateBuffer(playerUuid);
        deque.addFirst(entry);
        while (deque.size() > maxBufferSize) {
            deque.removeLast();
        }
        dirtyPlayers.add(playerUuid);
    }

    @Override
    public List<CommandEntry> getRecentCommands(UUID playerUuid, int limit) {
        if (playerUuid == null) return List.of();
        Deque<CommandEntry> deque = getOrCreateBuffer(playerUuid);
        return deque.stream().limit(Math.max(1, limit)).collect(Collectors.toList());
    }

    @Override
    public void clearLogs(UUID playerUuid) {
        if (playerUuid == null) return;
        Deque<CommandEntry> deque = getOrCreateBuffer(playerUuid);
        deque.clear();
        dirtyPlayers.add(playerUuid);
    }

    public void flush() {
        if (dirtyPlayers.isEmpty()) return;
        try {
            Files.createDirectories(logsDir);
        } catch (IOException ignored) {}

        Iterator<UUID> it = dirtyPlayers.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            it.remove();
            Deque<CommandEntry> deque = playerBuffers.get(uuid);
            if (deque == null) continue;

            Path playerFile = logsDir.resolve(uuid.toString() + ".json");
            Path tmp = logsDir.resolve(uuid.toString() + ".json.tmp");

            try {
                try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                    gson.toJson(new ArrayList<>(deque), writer);
                }
                try {
                    Files.move(tmp, playerFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp, playerFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed flushing command log for " + uuid + ": " + e.getMessage(), e);
            }
        }
    }
}
