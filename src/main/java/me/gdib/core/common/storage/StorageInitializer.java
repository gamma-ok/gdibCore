package me.gdib.core.common.storage;

import java.util.Objects;

import org.bukkit.plugin.java.JavaPlugin;

public final class StorageInitializer {

    private StorageInitializer() {
        throw new UnsupportedOperationException(
            "Utility class cannot be instantiated"
        );
    }

    public static StorageManager initialize(
            JavaPlugin plugin,
            StorageConfig config) {

        Objects.requireNonNull(
            plugin,
            "Plugin cannot be null"
        );

        Objects.requireNonNull(
            config,
            "StorageConfig cannot be null"
        );

        StorageProvider provider;

        switch (config.getType()) {

            case SQLITE:

                plugin.getLogger().info(
                    "Initializing SQLite storage provider..."
                );

                provider = new SQLiteProvider(
                    plugin,
                    config.getSQLiteFile()
                );

                break;

            case MYSQL:

                plugin.getLogger().info(
                    "Initializing MySQL storage provider..."
                );

                provider = new MySQLProvider(config);

                break;

            default:

                throw new IllegalStateException(
                    "Unsupported storage type: " + config.getType()
                );
        }

        StorageManager storageManager =
            new StorageManager(provider);

        plugin.getLogger().info(
            "Storage provider initialized successfully: "
            + config.getType()
        );

        return storageManager;
    }
}
