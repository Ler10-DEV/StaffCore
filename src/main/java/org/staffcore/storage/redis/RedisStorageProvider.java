package org.staffcore.storage.redis;

import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;

public class RedisStorageProvider implements StorageProvider {

    @Override
    public void initialize() throws StorageException {
        throw new UnsupportedOperationException(
                "Redis storage driver is a pluggable stub. Please refer to README.md to implement RedisDAO or contribute a PR."
        );
    }

    @Override
    public void shutdown() {}

    @Override
    public void flush() {}

    @Override
    public StorageType getType() {
        return StorageType.REDIS;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }

    @Override
    public LinkedAccountDAO linkedAccounts() {
        throw new UnsupportedOperationException("Redis driver not implemented");
    }

    @Override
    public PunishmentDAO punishments() {
        throw new UnsupportedOperationException("Redis driver not implemented");
    }

    @Override
    public KontRecordDAO kontRecords() {
        throw new UnsupportedOperationException("Redis driver not implemented");
    }

    @Override
    public StaffScoreDAO staffScores() {
        throw new UnsupportedOperationException("Redis driver not implemented");
    }

    @Override
    public CommandLogDAO commandLogs() {
        throw new UnsupportedOperationException("Redis driver not implemented");
    }
}
