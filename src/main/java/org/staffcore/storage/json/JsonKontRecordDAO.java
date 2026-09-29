package org.staffcore.storage.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.staffcore.storage.dao.KontRecordDAO;
import org.staffcore.storage.model.KontRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JsonKontRecordDAO implements KontRecordDAO {
    private final Path file;
    private final Gson gson;
    private final Logger logger;
    private final List<KontRecord> records = new CopyOnWriteArrayList<>();
    private static final int MAX_RECORDS = 1000;
    private volatile boolean dirty = false;

    public JsonKontRecordDAO(Path file, Gson gson, Logger logger) {
        this.file = file;
        this.gson = gson;
        this.logger = logger;
    }

    public void load() throws IOException {
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<KontRecord>>(){}.getType();
            List<KontRecord> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                records.clear();
                records.addAll(loaded);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load kont records from " + file + ": " + e.getMessage(), e);
        }
    }

    public void flush() {
        if (!dirty) return;
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                gson.toJson(records, writer);
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to flush kont records: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void addRecord(KontRecord record) {
        if (record == null) return;
        records.add(0, record); // Most recent first
        while (records.size() > MAX_RECORDS) {
            records.remove(records.size() - 1);
        }
        dirty = true;
    }

    @Override
    public List<KontRecord> getRecentRecords(int limit) {
        return records.stream().limit(Math.max(1, limit)).collect(Collectors.toList());
    }

    @Override
    public List<KontRecord> findByTarget(UUID targetUuid) {
        if (targetUuid == null) return List.of();
        return records.stream()
                .filter(r -> targetUuid.equals(r.getTargetUuid()))
                .collect(Collectors.toList());
    }

    @Override
    public List<KontRecord> findByStaff(UUID staffUuid) {
        if (staffUuid == null) return List.of();
        return records.stream()
                .filter(r -> staffUuid.equals(r.getStaffUuid()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<KontRecord> findById(String id) {
        if (id == null) return Optional.empty();
        return records.stream()
                .filter(r -> id.equalsIgnoreCase(r.getId()))
                .findFirst();
    }
}
