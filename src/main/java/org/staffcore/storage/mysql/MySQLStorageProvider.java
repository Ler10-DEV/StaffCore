package org.staffcore.storage.mysql;

import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;

/**
 * ==============================================================================
 * MYSQL STORAGE PROVIDER (PLUGGABLE DRIVER STUB)
 * ==============================================================================
 * To implement MySQL database driver:
 * 1. Add com.mysql:mysql-connector-j and com.zaxxer:HikariCP to pom.xml.
 * 2. Implement DAO interfaces using prepared statements and connection pooling.
 * 3. Set `storage.type: mysql` in config.yml.
 * 4. See docs in README.md.
 */
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
