package org.staffcore.storage.mysql;

import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;

public class MySQLStorageProvider implements StorageProvider {

    @Override
    public void initialize() throws StorageException {
        throw new UnsupportedOperationException(
                "MySQL storage driver is a pluggable stub. Please refer to README.md to implement MySQL DAO or contribute a PR."
        );
    }

    @Override
    public void shutdown() {}

    @Override
    public void flush() {}

    @Override
    public StorageType getType() {
        return StorageType.MYSQL;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }

    @Override
    public LinkedAccountDAO linkedAccounts() {
        throw new UnsupportedOperationException("MySQL driver not implemented");
    }

    @Override
    public PunishmentDAO punishments() {
        throw new UnsupportedOperationException("MySQL driver not implemented");
    }

    @Override
    public KontRecordDAO kontRecords() {
        throw new UnsupportedOperationException("MySQL driver not implemented");
    }

    @Override
    public StaffScoreDAO staffScores() {
        throw new UnsupportedOperationException("MySQL driver not implemented");
    }

    @Override
    public CommandLogDAO commandLogs() {
        throw new UnsupportedOperationException("MySQL driver not implemented");
    }
}
