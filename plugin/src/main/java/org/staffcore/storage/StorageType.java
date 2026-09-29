package org.staffcore.storage;

public enum StorageType {
    JSON,
    REDIS,
    H2,
    MYSQL,
    POSTGRESQL;

    public static StorageType fromString(String name) {
        if (name == null) return JSON;
        try {
            return StorageType.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return JSON;
        }
    }
}
