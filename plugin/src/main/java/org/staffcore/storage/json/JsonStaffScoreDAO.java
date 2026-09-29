package org.staffcore.storage.json;

import com.google.gson.Gson;
import org.staffcore.storage.dao.StaffScoreDAO;
import org.staffcore.storage.model.ScoreEvent;
import org.staffcore.storage.model.ScoreRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JsonStaffScoreDAO implements StaffScoreDAO {
    private final JsonStore<ScoreRecord> store;
    private final Path auditFile;
    private final Gson gson;
    private final Logger logger;
    private final List<ScoreEvent> recentEvents = Collections.synchronizedList(new ArrayList<>());

    public JsonStaffScoreDAO(JsonStore<ScoreRecord> store, Path auditFile, Gson gson, Logger logger) {
        this.store = store;
        this.auditFile = auditFile;
        this.gson = gson;
        this.logger = logger;
        loadRecentEvents();
    }

    private void loadRecentEvents() {
        if (!Files.exists(auditFile)) return;
        try (BufferedReader reader = Files.newBufferedReader(auditFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    ScoreEvent event = gson.fromJson(line, ScoreEvent.class);
                    if (event != null) {
                        recentEvents.add(event);
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed reading score audit events: " + e.getMessage());
        }
    }

    @Override
    public Optional<ScoreRecord> findByStaffUuid(UUID staffUuid) {
        if (staffUuid == null) return Optional.empty();
        return Optional.ofNullable(store.get(staffUuid.toString()));
    }

    @Override
    public Collection<ScoreRecord> getAll() {
        return store.values();
    }

    @Override
    public void save(ScoreRecord record) {
        if (record == null || record.getStaffUuid() == null) return;
        store.put(record.getStaffUuid().toString(), record);
    }

    @Override
    public List<ScoreRecord> getLeaderboard(boolean weeklyOnly, int limit) {
        Comparator<ScoreRecord> comp = weeklyOnly
                ? Comparator.comparingInt(ScoreRecord::getWeeklyScore).reversed()
                : Comparator.comparingInt(ScoreRecord::getTotalScore).reversed();

        return store.values().stream()
                .sorted(comp)
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());
    }

    @Override
    public void resetWeeklyScores() {
        for (ScoreRecord record : store.values()) {
            record.resetWeeklyScore();
            store.put(record.getStaffUuid().toString(), record);
        }
    }

    @Override
    public void logEvent(ScoreEvent event) {
        if (event == null) return;
        recentEvents.add(event);

        // Append to score_events.jsonl
        try {
            if (auditFile.getParent() != null) Files.createDirectories(auditFile.getParent());
            String jsonLine = gson.toJson(event) + "\n";
            Files.writeString(auditFile, jsonLine, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to append score audit event: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ScoreEvent> getEventsForStaff(UUID staffUuid, int limit) {
        if (staffUuid == null) return List.of();
        synchronized (recentEvents) {
            return recentEvents.stream()
                    .filter(e -> staffUuid.equals(e.getStaffUuid()))
                    .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                    .limit(Math.max(1, limit))
                    .collect(Collectors.toList());
        }
    }
}
