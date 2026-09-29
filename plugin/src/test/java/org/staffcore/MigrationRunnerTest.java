package org.staffcore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.staffcore.storage.MigrationRunner;
import org.staffcore.storage.json.JsonStorageProvider;
import org.staffcore.storage.model.LinkRecord;
import org.staffcore.storage.model.Punishment;
import org.staffcore.storage.model.PunishmentStatus;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MigrationRunnerTest {

    @TempDir
    Path tempDir;

    @Test
    void testMigrationDryRunAndExecute() throws Exception {
        Path srcData = tempDir.resolve("src_data");
        Path srcBack = tempDir.resolve("src_backups");
        Path dstData = tempDir.resolve("dst_data");
        Path dstBack = tempDir.resolve("dst_backups");

        JsonStorageProvider source = new JsonStorageProvider(srcData, srcBack, false, Logger.getLogger("SrcLogger"));
        source.initialize();

        JsonStorageProvider target = new JsonStorageProvider(dstData, dstBack, false, Logger.getLogger("DstLogger"));
        target.initialize();

        // Populate source
        UUID u1 = UUID.randomUUID();
        source.linkedAccounts().save(new LinkRecord(u1, "Alice", "111222", "Alice#0001", true, System.currentTimeMillis()));
        source.punishments().save(new Punishment("#CZ-1001", u1, "Alice", UUID.randomUUID(), "Admin", "Hacking", "BAN", System.currentTimeMillis(), -1, PunishmentStatus.VERIFIED, "http://proof.com", "Confirmed", System.currentTimeMillis()));
        source.flush();

        MigrationRunner runner = new MigrationRunner(source, Logger.getLogger("TestMigration"));

        // Dry Run
        MigrationRunner.MigrationReport dryReport = runner.migrate(target, true, srcData);
        assertTrue(dryReport.dryRun());
        assertEquals(2, dryReport.totalMigrated());

        // Actual Migration
        MigrationRunner.MigrationReport actualReport = runner.migrate(target, false, srcData);
        assertEquals(2, actualReport.totalMigrated());

        // Verify target has data
        assertTrue(target.linkedAccounts().findByUuid(u1).isPresent());
        assertTrue(target.punishments().findById("#CZ-1001").isPresent());
    }
}
