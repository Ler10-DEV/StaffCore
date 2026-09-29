package org.staffcore.storage.json;

import org.staffcore.storage.dao.LinkedAccountDAO;
import org.staffcore.storage.model.LinkRecord;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class JsonLinkedAccountDAO implements LinkedAccountDAO {
    private final JsonStore<LinkRecord> store;

    public JsonLinkedAccountDAO(JsonStore<LinkRecord> store) {
        this.store = store;
    }

    @Override
    public Optional<LinkRecord> findByUuid(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(store.get(uuid.toString()));
    }

    @Override
    public Optional<LinkRecord> findByDiscordId(String discordId) {
        if (discordId == null) return Optional.empty();
        return store.values().stream()
                .filter(record -> discordId.equals(record.getDiscordId()))
                .findFirst();
    }

    @Override
    public Collection<LinkRecord> getAll() {
        return store.values();
    }

    @Override
    public void save(LinkRecord record) {
        if (record == null || record.getUuid() == null) return;
        store.put(record.getUuid().toString(), record);
    }

    @Override
    public void delete(UUID uuid) {
        if (uuid == null) return;
        store.remove(uuid.toString());
    }

    @Override
    public boolean isStaff(UUID uuid) {
        if (uuid == null) return false;
        LinkRecord record = store.get(uuid.toString());
        return record != null && record.isStaff();
    }

    @Override
    public void setStaff(UUID uuid, boolean staff) {
        if (uuid == null) return;
        LinkRecord existing = store.get(uuid.toString());
        if (existing != null) {
            store.put(uuid.toString(), existing.withStaff(staff));
        }
    }
}
