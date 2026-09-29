package org.staffcore.storage.dao;

import org.staffcore.storage.model.LinkRecord;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for player-to-Discord linked accounts.
 */
public interface LinkedAccountDAO {
    Optional<LinkRecord> findByUuid(UUID uuid);
    Optional<LinkRecord> findByDiscordId(String discordId);
    Collection<LinkRecord> getAll();
    void save(LinkRecord record);
    void delete(UUID uuid);
    boolean isStaff(UUID uuid);
    void setStaff(UUID uuid, boolean staff);
}
