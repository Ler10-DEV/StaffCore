package org.staffcore.storage.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonStore<T> {
    private final Path file;
    private final Gson gson;
    private final Type itemType;
    private final Logger logger;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    public JsonStore(Path file, Gson gson, Type itemType, Logger logger) {
        this.file = file;
        this.gson = gson;
        this.itemType = itemType;
        this.logger = logger;
    }

public T get(String key) {
        return cache.get(key);
    }

public Collection<T> values() {
        return List.copyOf(cache.values());
    }

public Map<String, T> asMap() {
        return Map.copyOf(cache);
    }

public void put(String key, T value) {
        cache.put(key, value);
        dirty = true;
    }

public void remove(String key) {
        cache.remove(key);
        dirty = true;
    }

public void clear() {
        cache.clear();
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

public void flush() {
        if (!dirty) return;
        lock.writeLock().lock();
        try {
            if (!dirty) return;
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }

            Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                gson.toJson(cache, writer);
            }

            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }

            dirty = false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "JSON Store flush failed for " + file.getFileName() + ": " + e.getMessage(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

public void load() throws IOException {
        lock.writeLock().lock();
        try {
            if (!Files.exists(file)) {
                return;
            }

            Type mapType = TypeToken.getParameterized(Map.class, String.class, itemType).getType();
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                Map<String, T> loaded = gson.fromJson(reader, mapType);
                if (loaded != null) {
                    cache.clear();
                    cache.putAll(loaded);
                }
                dirty = false;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Detected corrupted JSON file at " + file + ". Backing up and resetting.", e);
                Path corruptBackup = file.resolveSibling(file.getFileName().toString() + ".corrupt-" + System.currentTimeMillis());
                try {
                    Files.copy(file, corruptBackup, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {}
                cache.clear();
                dirty = true;
                flush();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
