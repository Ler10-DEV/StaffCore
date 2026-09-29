package org.staffcore.storage.redis;

import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;

/**
 * ==============================================================================
 * REDIS STORAGE PROVIDER (PLUGGABLE DRIVER STUB)
 * ==============================================================================
 * To implement Redis backend for multi-server / BungeeCord / Velocity networks:
 * 1. Add Jedis or Lettuce dependency in pom.xml.
 * 2. Implement the 5 DAO interfaces (LinkedAccountDAO, PunishmentDAO, KontRecordDAO,
 *    StaffScoreDAO, CommandLogDAO) using Redis Hashes, PubSub, and Sorted Sets.
 * 3. Update initialize() to connect using pool settings from config.yml.
 * 4. See docs in README.md under "Database Migration & Storage Providers".
 */
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
