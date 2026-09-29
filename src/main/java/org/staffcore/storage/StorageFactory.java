package org.staffcore.storage;

import org.staffcore.storage.h2.H2StorageProvider;
import org.staffcore.storage.json.JsonStorageProvider;
import org.staffcore.storage.mysql.MySQLStorageProvider;
import org.staffcore.storage.postgres.PostgresStorageProvider;
import org.staffcore.storage.redis.RedisStorageProvider;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class StorageFactory {
    private static final Map<StorageType, Supplier<StorageProvider>> REGISTRY = new ConcurrentHashMap<>();

    public static void registerProvider(StorageType type, Supplier<StorageProvider> supplier) {
        REGISTRY.put(type, supplier);
    }

    public static StorageProvider createProvider(StorageType type, Path dataDir, Path backupDir, boolean prettyPrint, Logger logger) {
        if (REGISTRY.containsKey(type)) {
            return REGISTRY.get(type).get();
        }

        return switch (type) {
            case JSON -> new JsonStorageProvider(dataDir, backupDir, prettyPrint, logger);
            case REDIS -> new RedisStorageProvider();
            case H2 -> new H2StorageProvider();
            case MYSQL -> new MySQLStorageProvider();
            case POSTGRESQL -> new PostgresStorageProvider();
        };
    }
}
