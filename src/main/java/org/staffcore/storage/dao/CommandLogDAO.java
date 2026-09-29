package org.staffcore.storage.dao;

import org.staffcore.storage.model.CommandEntry;

import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for player command logs (per-player storage).
 */
public interface CommandLogDAO {
    void logCommand(UUID playerUuid, CommandEntry entry);
    List<CommandEntry> getRecentCommands(UUID playerUuid, int limit);
    void clearLogs(UUID playerUuid);
}
