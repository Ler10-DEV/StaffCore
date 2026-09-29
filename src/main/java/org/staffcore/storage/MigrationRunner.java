package org.staffcore.storage;

import org.staffcore.storage.model.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class MigrationRunner {
    private final StorageProvider sourceProvider;
    private final Logger logger;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public record MigrationReport(int linkCount, int punishmentCount, int kontCount, int scoreCount, int totalMigrated, boolean dryRun, String message) {}

    public MigrationRunner(StorageProvider sourceProvider, Logger logger) {
        this.sourceProvider = sourceProvider;
        this.logger = logger;
    }

public MigrationReport migrate(StorageProvider targetProvider, boolean dryRun, Path sourceDataDir) throws Exception {
        Collection<LinkRecord> links = sourceProvider.linkedAccounts().getAll();
        Collection<Punishment> punishments = sourceProvider.punishments().getAll();
        List<KontRecord> konts = sourceProvider.kontRecords().getRecentRecords(10000);
        Collection<ScoreRecord> scores = sourceProvider.staffScores().getAll();

        int linkCount = links.size();
        int punishmentCount = punishments.size();
        int kontCount = konts.size();
        int scoreCount = scores.size();
        int total = linkCount + punishmentCount + kontCount + scoreCount;

        if (dryRun) {
            String msg = String.format("Dry-run migration analysis: %d links, %d punishments, %d kont records, %d score records (Total: %d)",
                    linkCount, punishmentCount, kontCount, scoreCount, total);
            logger.info(msg);
            return new MigrationReport(linkCount, punishmentCount, kontCount, scoreCount, total, true, msg);
        }

        logger.info("Starting migration of " + total + " records to " + targetProvider.getType() + "...");

targetProvider.initialize();

for (LinkRecord link : links) {
            targetProvider.linkedAccounts().save(link);
        }
        for (Punishment p : punishments) {
            targetProvider.punishments().save(p);
        }
        for (KontRecord k : konts) {
            targetProvider.kontRecords().addRecord(k);
        }
        for (ScoreRecord s : scores) {
            targetProvider.staffScores().save(s);
        }

        targetProvider.flush();

if (sourceDataDir != null && Files.exists(sourceDataDir)) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            Path archiveDir = sourceDataDir.resolveSibling("_migrated_" + timestamp);
            Files.createDirectories(archiveDir);

            try (var stream = Files.list(sourceDataDir)) {
                stream.forEach(file -> {
                    try {
                        if (!file.getFileName().toString().startsWith("_migrated_")) {
                            Files.copy(file, archiveDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        logger.warning("Failed to archive migrated file: " + file.getFileName());
                    }
                });
            }
            logger.info("Migrated source data archived to " + archiveDir);
        }

        String msg = String.format("Successfully migrated %d records to %s.", total, targetProvider.getType());
        logger.info(msg);
        return new MigrationReport(linkCount, punishmentCount, kontCount, scoreCount, total, false, msg);
    }
}
