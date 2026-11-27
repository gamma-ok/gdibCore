package me.gdib.core.common.storage;

import java.util.Objects;

import org.bukkit.configuration.file.FileConfiguration;

import me.gdib.core.common.config.ConfigManager;

public final class StorageConfig {

    private final ConfigManager configManager;

    private StorageType type;

    private String sqliteFile;

    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private boolean mysqlUseSSL;

    public StorageConfig(ConfigManager configManager) {

        this.configManager = Objects.requireNonNull(
            configManager,
            "ConfigManager cannot be null"
        );

        load();
    }

    public void load() {

        FileConfiguration config =
            configManager.loadConfig("config.yml");

        loadValues(config);
    }

    public void reload() {

        configManager.reloadConfig("config.yml");

        FileConfiguration config =
            configManager.getConfig("config.yml");

        loadValues(config);
    }

    private void loadValues(FileConfiguration config) {

        // Storage type.
        this.type = StorageType.fromString(
            config.getString("storage.type", "SQLITE")
        );

        // SQLite configuration.
        this.sqliteFile = config.getString(
            "storage.sqlite.file",
            "database.db"
        );

        if (sqliteFile == null || sqliteFile.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "SQLite database file cannot be empty"
            );
        }

        // MySQL configuration.
        this.mysqlHost = config.getString(
            "storage.mysql.host",
            "localhost"
        );

        this.mysqlPort = config.getInt(
            "storage.mysql.port",
            3306
        );

        this.mysqlDatabase = config.getString(
            "storage.mysql.database",
            "gdibcore"
        );

        this.mysqlUsername = config.getString(
            "storage.mysql.username",
            "root"
        );

        this.mysqlPassword = config.getString(
            "storage.mysql.password",
            ""
        );

        this.mysqlUseSSL = config.getBoolean(
            "storage.mysql.useSSL",
            false
        );

        validateMySQLConfiguration();
    }

    private void validateMySQLConfiguration() {

        if (type != StorageType.MYSQL) {
            return;
        }

        if (mysqlHost == null || mysqlHost.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "MySQL host cannot be empty"
            );
        }

        if (mysqlPort < 1 || mysqlPort > 65535) {
            throw new IllegalArgumentException(
                "MySQL port must be between 1 and 65535"
            );
        }

        if (mysqlDatabase == null
                || mysqlDatabase.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "MySQL database cannot be empty"
            );
        }

        if (mysqlUsername == null
                || mysqlUsername.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "MySQL username cannot be empty"
            );
        }
    }

    public StorageType getType() {
        return type;
    }

    public String getSQLiteFile() {
        return sqliteFile;
    }

    public String getMySQLHost() {
        return mysqlHost;
    }

    public int getMySQLPort() {
        return mysqlPort;
    }

    public String getMySQLDatabase() {
        return mysqlDatabase;
    }

    public String getMySQLUsername() {
        return mysqlUsername;
    }

    public String getMySQLPassword() {
        return mysqlPassword;
    }

    public boolean isMySQLUseSSL() {
        return mysqlUseSSL;
    }
}
