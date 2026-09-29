package org.staffcore.storage;

import org.staffcore.storage.dao.*;

/**
 * Pluggable Storage Provider Contract.
 * All storage backends (JSON, Redis, H2, MySQL, PostgreSQL) must implement this interface.
 */
public interface StorageProvider extends AutoCloseable {
    void initialize() throws StorageException;
    void shutdown();
    default void close() {
        shutdown();
    }
    void flush();
    StorageType getType();
    boolean isHealthy();

    LinkedAccountDAO linkedAccounts();
    PunishmentDAO punishments();
    KontRecordDAO kontRecords();
    StaffScoreDAO staffScores();
    CommandLogDAO commandLogs();
}
