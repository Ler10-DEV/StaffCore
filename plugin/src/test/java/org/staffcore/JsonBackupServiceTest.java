package org.staffcore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.staffcore.storage.json.JsonBackupService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonBackupServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void testBackupCreation() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Path backupDir = tempDir.resolve("backups");
        Files.createDirectories(dataDir);

        Files.writeString(dataDir.resolve("sample.json"), "{\"hello\":\"world\"}");

        JsonBackupService service = new JsonBackupService(dataDir, backupDir, 7, Logger.getLogger("TestLogger"));
        Path zip = service.createBackupNow();

        assertTrue(Files.exists(zip));
        assertTrue(zip.getFileName().toString().startsWith("data-"));
        assertTrue(zip.getFileName().toString().endsWith(".zip"));
    }
}
