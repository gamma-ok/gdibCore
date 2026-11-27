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

        FileConfiguration config = Objects.requireNonNull(
            configManager.loadConfig("config.yml"),
            "Configuration cannot be null"
        );

        loadValues(config);
    }

    public void reload() {

        configManager.reloadConfig("config.yml");

        FileConfiguration config = Objects.requireNonNull(
            configManager.getConfig("config.yml"),
            "Configuration cannot be null"
        );

        loadValues(config);
    }

    private void loadValues(FileConfiguration config) {

        Objects.requireNonNull(
            config,
            "Configuration cannot be null"
        );

        // Read storage type.
        StorageType newType = StorageType.fromString(
            config.getString("storage.type", "SQLITE")
        );

        // Read SQLite configuration.
        String newSQLiteFile = config.getString(
            "storage.sqlite.file",
            "database.db"
        );

        // Read MySQL configuration.
        String newMySQLHost = config.getString(
            "storage.mysql.host",
            "localhost"
        );

        int newMySQLPort = config.getInt(
            "storage.mysql.port",
            3306
        );

        String newMySQLDatabase = config.getString(
            "storage.mysql.database",
            "gdibcore"
        );

        String newMySQLUsername = config.getString(
            "storage.mysql.username",
            "root"
        );

        String newMySQLPassword = config.getString(
            "storage.mysql.password",
            ""
        );

        boolean newMySQLUseSSL = config.getBoolean(
            "storage.mysql.useSSL",
            false
        );

        // Validate values before modifying current state.
        validateSQLiteConfiguration(newSQLiteFile);

        validateMySQLConfiguration(
            newType,
            newMySQLHost,
            newMySQLPort,
            newMySQLDatabase,
            newMySQLUsername
        );

        // Apply configuration only after successful validation.
        this.type = newType;
        this.sqliteFile = newSQLiteFile;

        this.mysqlHost = newMySQLHost;
        this.mysqlPort = newMySQLPort;
        this.mysqlDatabase = newMySQLDatabase;
        this.mysqlUsername = newMySQLUsername;
        this.mysqlPassword = newMySQLPassword;
        this.mysqlUseSSL = newMySQLUseSSL;
    }

    private void validateSQLiteConfiguration(String file) {

        if (file == null || file.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "SQLite database file cannot be empty"
            );
        }
    }

    private void validateMySQLConfiguration(
            StorageType type,
            String host,
            int port,
            String database,
            String username) {

        if (type != StorageType.MYSQL) {
            return;
        }

        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "MySQL host cannot be empty"
            );
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                "MySQL port must be between 1 and 65535"
            );
        }

        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "MySQL database cannot be empty"
            );
        }

        if (username == null || username.trim().isEmpty()) {
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
