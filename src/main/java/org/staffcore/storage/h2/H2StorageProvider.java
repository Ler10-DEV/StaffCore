package org.staffcore.storage.h2;

import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;

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
