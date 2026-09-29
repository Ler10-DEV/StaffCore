package org.staffcore.storage;

import org.staffcore.storage.dao.*;

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
