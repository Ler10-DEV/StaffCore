package org.staffcore;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.staffcore.storage.json.JsonStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class JsonStoreTest {

    @TempDir
    Path tempDir;

    private JsonStore<String> store;
    private Path storeFile;

    @BeforeEach
    void setUp() throws IOException {
        storeFile = tempDir.resolve("test_store.json");
        store = new JsonStore<>(storeFile, new Gson(), String.class, Logger.getLogger("TestLogger"));
        store.load();
    }

    @Test
    void testPutGetAndFlush() throws IOException {
        store.put("key1", "value1");
        store.put("key2", "value2");

        assertEquals("value1", store.get("key1"));
        assertEquals("value2", store.get("key2"));
        assertTrue(store.isDirty());

        store.flush();
        assertFalse(store.isDirty());
        assertTrue(Files.exists(storeFile));

        // Reload fresh from disk
        JsonStore<String> reloaded = new JsonStore<>(storeFile, new Gson(), String.class, Logger.getLogger("TestLogger"));
        reloaded.load();

        assertEquals("value1", reloaded.get("key1"));
        assertEquals("value2", reloaded.get("key2"));
    }

    @Test
    void testCorruptedFileRecovery() throws IOException {
        Files.writeString(storeFile, "INVALID JSON CONTENT { [ @@@");
        assertTrue(Files.exists(storeFile));

        JsonStore<String> storeWithCorruptFile = new JsonStore<>(storeFile, new Gson(), String.class, Logger.getLogger("TestLogger"));
        assertDoesNotThrow(storeWithCorruptFile::load);

        // Should recover gracefully with empty cache and preserve data
        assertNull(storeWithCorruptFile.get("any_key"));
        storeWithCorruptFile.put("valid", "data");
        storeWithCorruptFile.flush();

        assertEquals("data", storeWithCorruptFile.get("valid"));
    }
}
