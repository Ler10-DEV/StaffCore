package org.staffcore.storage.h2;

import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;

/**
 * ==============================================================================
 * H2 DATABASE STORAGE PROVIDER (PLUGGABLE DRIVER STUB)
 * ==============================================================================
 * To implement embedded H2 SQL database:
 * 1. Add com.h2database:h2 dependency to pom.xml.
 * 2. Create JDBC schema and implement DAO interfaces with HikariCP pool.
 * 3. Switch `storage.type: h2` in config.yml.
 * 4. See docs in README.md.
 */
public class H2StorageProvider implements StorageProvider {

    @Override
    public void initialize() throws StorageException {
        throw new UnsupportedOperationException(
                "H2 database driver is a pluggable stub. Please refer to README.md to implement H2 DAO or contribute a PR."
        );
    }

    @Override
    public void shutdown() {}

    @Override
    public void flush() {}

    @Override
    public StorageType getType() {
        return StorageType.H2;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }

    @Override
    public LinkedAccountDAO linkedAccounts() {
        throw new UnsupportedOperationException("H2 driver not implemented");
    }

    @Override
    public PunishmentDAO punishments() {
        throw new UnsupportedOperationException("H2 driver not implemented");
    }

    @Override
    public KontRecordDAO kontRecords() {
        throw new UnsupportedOperationException("H2 driver not implemented");
    }

    @Override
    public StaffScoreDAO staffScores() {
        throw new UnsupportedOperationException("H2 driver not implemented");
    }

    @Override
    public CommandLogDAO commandLogs() {
        throw new UnsupportedOperationException("H2 driver not implemented");
    }
}
