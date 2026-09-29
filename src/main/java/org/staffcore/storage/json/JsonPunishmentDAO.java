package org.staffcore.storage.json;

import org.staffcore.storage.dao.PunishmentDAO;
import org.staffcore.storage.model.Punishment;
import org.staffcore.storage.model.PunishmentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JsonPunishmentDAO implements PunishmentDAO {
    private final JsonStore<Punishment> store;
    private final JsonStore<String> metaStore;
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public JsonPunishmentDAO(JsonStore<Punishment> store, JsonStore<String> metaStore) {
        this.store = store;
        this.metaStore = metaStore;
        initializeSequence();
    }

    private void initializeSequence() {
        String lastSeqStr = metaStore.get("last_punishment_seq");
        if (lastSeqStr != null) {
            try {
                sequence.set(Integer.parseInt(lastSeqStr));
            } catch (NumberFormatException ignored) {}
        } else {
            // Find highest from existing punishments
            int max = 1000;
            for (Punishment p : store.values()) {
                String id = p.getId();
                if (id != null && id.contains("CZ-")) {
                    try {
                        String numStr = id.replaceAll("[^0-9]", "");
                        int num = Integer.parseInt(numStr);
                        if (num > max) max = num;
                    } catch (Exception ignored) {}
                }
            }
            sequence.set(max);
        }
    }

    @Override
    public synchronized String generateNextId() {
        int next = sequence.incrementAndGet();
        metaStore.put("last_punishment_seq", String.valueOf(next));
        return String.format("#CZ-%04d", next);
    }

    @Override
    public Optional<Punishment> findById(String id) {
        if (id == null) return Optional.empty();
        String normalized = id.startsWith("#") ? id : "#" + id;
        Punishment p = store.get(normalized);
        if (p == null) {
            // Fallback check without #
            p = store.get(id);
        }
        return Optional.ofNullable(p);
    }

    @Override
    public Collection<Punishment> getAll() {
        return store.values();
    }

    @Override
    public List<Punishment> findByTarget(UUID targetUuid) {
        if (targetUuid == null) return List.of();
        return store.values().stream()
                .filter(p -> targetUuid.equals(p.getTargetUuid()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Punishment> findByStaff(UUID staffUuid) {
        if (staffUuid == null) return List.of();
        return store.values().stream()
                .filter(p -> staffUuid.equals(p.getStaffUuid()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Punishment> findByStatus(PunishmentStatus status) {
        if (status == null) return List.of();
        return store.values().stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Punishment punishment) {
        if (punishment == null || punishment.getId() == null) return;
        store.put(punishment.getId(), punishment);
    }

    @Override
    public void delete(String id) {
        if (id == null) return;
        store.remove(id);
        if (id.startsWith("#")) {
            store.remove(id.substring(1));
        } else {
            store.remove("#" + id);
        }
    }
}
