package org.staffcore.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.staffcore.storage.StorageException;
import org.staffcore.storage.StorageProvider;
import org.staffcore.storage.StorageType;
import org.staffcore.storage.dao.*;
import org.staffcore.storage.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonStorageProvider implements StorageProvider {
    private final Path dataDir;
    private final Path backupDir;
    private final boolean prettyPrint;
    private final Logger logger;

    private Gson gson;
    private JsonStore<LinkRecord> linkStore;
    private JsonStore<Punishment> punishmentStore;
    private JsonStore<String> metaStore;
    private JsonStore<ScoreRecord> scoreStore;

    private JsonLinkedAccountDAO linkedAccountDAO;
    private JsonPunishmentDAO punishmentDAO;
    private JsonKontRecordDAO kontRecordDAO;
    private JsonStaffScoreDAO staffScoreDAO;
    private JsonCommandLogDAO commandLogDAO;
    private JsonBackupService backupService;

    private volatile boolean initialized = false;

    public JsonStorageProvider(Path dataDir, Path backupDir, boolean prettyPrint, Logger logger) {
        this.dataDir = dataDir;
        this.backupDir = backupDir;
        this.prettyPrint = prettyPrint;
        this.logger = logger;
    }

    @Override
    public void initialize() throws StorageException {
        try {
            Files.createDirectories(dataDir);
            Files.createDirectories(backupDir);

            GsonBuilder builder = new GsonBuilder();
            if (prettyPrint) {
                builder.setPrettyPrinting();
            }
            this.gson = builder.create();

            this.linkStore = new JsonStore<>(dataDir.resolve("linked_accounts.json"), gson, LinkRecord.class, logger);
            this.punishmentStore = new JsonStore<>(dataDir.resolve("punishments.json"), gson, Punishment.class, logger);
            this.metaStore = new JsonStore<>(dataDir.resolve("staffcore.meta.json"), gson, String.class, logger);
            this.scoreStore = new JsonStore<>(dataDir.resolve("staff_scores.json"), gson, ScoreRecord.class, logger);

            linkStore.load();
            punishmentStore.load();
            metaStore.load();
            scoreStore.load();

            this.linkedAccountDAO = new JsonLinkedAccountDAO(linkStore);
            this.punishmentDAO = new JsonPunishmentDAO(punishmentStore, metaStore);

            this.kontRecordDAO = new JsonKontRecordDAO(dataDir.resolve("kont_records.json"), gson, logger);
            kontRecordDAO.load();

            this.staffScoreDAO = new JsonStaffScoreDAO(scoreStore, dataDir.resolve("score_events.jsonl"), gson, logger);
            this.commandLogDAO = new JsonCommandLogDAO(dataDir.resolve("command_logs"), gson, logger, 54);
            this.backupService = new JsonBackupService(dataDir, backupDir, 7, logger);

            this.initialized = true;
            logger.info("JSON Storage Engine initialized successfully at " + dataDir);
        } catch (IOException e) {
            throw new StorageException("Failed to initialize JSON Storage Engine", e);
        }
    }

    @Override
    public void shutdown() {
        if (!initialized) return;
        flush();
        logger.info("JSON Storage Engine shut down cleanly.");
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public void flush() {
        if (!initialized) return;
        try {
            if (linkStore != null) linkStore.flush();
            if (punishmentStore != null) punishmentStore.flush();
            if (metaStore != null) metaStore.flush();
            if (scoreStore != null) scoreStore.flush();
            if (kontRecordDAO != null) kontRecordDAO.flush();
            if (commandLogDAO != null) commandLogDAO.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while flushing JSON stores: " + e.getMessage(), e);
        }
    }

    @Override
    public StorageType getType() {
        return StorageType.JSON;
    }

    @Override
    public boolean isHealthy() {
        return initialized && Files.exists(dataDir) && Files.isWritable(dataDir);
    }

    @Override
    public LinkedAccountDAO linkedAccounts() {
        return linkedAccountDAO;
    }

    @Override
    public PunishmentDAO punishments() {
        return punishmentDAO;
    }

    @Override
    public KontRecordDAO kontRecords() {
        return kontRecordDAO;
    }

    @Override
    public StaffScoreDAO staffScores() {
        return staffScoreDAO;
    }

    @Override
    public CommandLogDAO commandLogs() {
        return commandLogDAO;
    }

    public JsonBackupService getBackupService() {
        return backupService;
    }

    public Path getDataDir() {
        return dataDir;
    }
}
