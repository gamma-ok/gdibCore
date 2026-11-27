package me.gdib.core.common.storage;

public enum StorageType {

    SQLITE,
    MYSQL;

    public static StorageType fromString(String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Storage type cannot be null or empty"
            );
        }

        try {
            return StorageType.valueOf(
                value.trim().toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                "Unsupported storage type: " + value
                + ". Supported types: SQLITE, MYSQL.",
                exception
            );
        }
    }
}
