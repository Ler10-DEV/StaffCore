package org.staffcore.storage.json;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages automated and on-demand zip backups of the data directory.
 */
public class JsonBackupService {
    private final Path dataDir;
    private final Path backupDir;
    private final int retentionDays;
    private final Logger logger;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public JsonBackupService(Path dataDir, Path backupDir, int retentionDays, Logger logger) {
        this.dataDir = dataDir;
        this.backupDir = backupDir;
        this.retentionDays = retentionDays > 0 ? retentionDays : 7;
        this.logger = logger;
    }

    /**
     * Creates a compressed zip backup of the data folder immediately.
     *
     * @return Path to the created zip file
     * @throws IOException If zip creation fails
     */
    public synchronized Path createBackupNow() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        Files.createDirectories(backupDir);

        String timestamp = LocalDateTime.now().format(FORMATTER);
        Path zipFile = backupDir.resolve("data-" + timestamp + ".zip");
        Path tmpZip = backupDir.resolve("data-" + timestamp + ".zip.tmp");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZip))) {
            Files.walkFileTree(dataDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Avoid copying .tmp files
                    if (file.getFileName().toString().endsWith(".tmp")) {
                        return FileVisitResult.CONTINUE;
                    }
                    String relPath = dataDir.relativize(file).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(relPath));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        Files.move(tmpZip, zipFile, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Created backup: " + zipFile.getFileName());

        purgeOldBackups();
        return zipFile;
    }

    /**
     * Purges backup archives older than the configured retention days.
     */
    public void purgeOldBackups() {
        if (!Files.exists(backupDir)) return;
        long cutoffMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays);

        try (Stream<Path> stream = Files.list(backupDir)) {
            stream.filter(p -> p.getFileName().toString().startsWith("data-") && p.getFileName().toString().endsWith(".zip"))
                    .forEach(p -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                            if (attrs.creationTime().toMillis() < cutoffMillis || attrs.lastModifiedTime().toMillis() < cutoffMillis) {
                                Files.deleteIfExists(p);
                                logger.info("Purged old backup: " + p.getFileName());
                            }
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Failed to check/delete old backup " + p.getFileName() + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to list backups for purging: " + e.getMessage());
        }
    }
}
