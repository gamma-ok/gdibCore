package me.gdib.core.common.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class MySQLProvider implements StorageProvider {

    private final StorageConfig config;
    private final ExecutorService executor;

    private Connection connection;

    private volatile boolean closed;

    private CompletableFuture<Void> closeFuture;

    public MySQLProvider(StorageConfig config) {

        this.config = Objects.requireNonNull(
            config,
            "StorageConfig cannot be null"
        );

        if (config.getType() != StorageType.MYSQL) {
            throw new IllegalArgumentException(
                "Storage configuration must use MYSQL"
            );
        }

        ThreadFactory threadFactory = runnable -> {

            Thread thread = new Thread(
                runnable,
                "gdibCore-MySQL"
            );

            thread.setDaemon(true);

            return thread;
        };

        this.executor = Executors.newSingleThreadExecutor(
            threadFactory
        );

        this.closed = false;
    }

    @Override
    public synchronized CompletableFuture<Void> save(
            String key,
            String value) {

        validateKey(key);
        Objects.requireNonNull(value, "Value cannot be null");

        return submit(conn -> {

            String sql =
                "INSERT INTO core_data (data_key, data_value) "
                + "VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE data_value = ?";

            try (PreparedStatement statement =
                    conn.prepareStatement(sql)) {

                statement.setString(1, key);
                statement.setString(2, value);
                statement.setString(3, value);

                statement.executeUpdate();
            }

            return null;
        });
    }

    @Override
    public synchronized CompletableFuture<Optional<String>> load(
            String key) {

        validateKey(key);

        return submit(conn -> {

            String sql =
                "SELECT data_value FROM core_data "
                + "WHERE data_key = ?";

            try (PreparedStatement statement =
                    conn.prepareStatement(sql)) {

                statement.setString(1, key);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return Optional.of(
                            result.getString("data_value")
                        );
                    }
                }
            }

            return Optional.empty();
        });
    }

    @Override
    public synchronized CompletableFuture<Boolean> exists(
            String key) {

        validateKey(key);

        return submit(conn -> {

            String sql =
                "SELECT 1 FROM core_data WHERE data_key = ?";

            try (PreparedStatement statement =
                    conn.prepareStatement(sql)) {

                statement.setString(1, key);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        });
    }

    @Override
    public synchronized CompletableFuture<Void> delete(
            String key) {

        validateKey(key);

        return submit(conn -> {

            String sql =
                "DELETE FROM core_data WHERE data_key = ?";

            try (PreparedStatement statement =
                    conn.prepareStatement(sql)) {

                statement.setString(1, key);

                statement.executeUpdate();
            }

            return null;
        });
    }

    private synchronized <T> CompletableFuture<T> submit(
            MySQLOperation<T> operation) {

        if (closed) {
            throw new IllegalStateException(
                "MySQL provider is already closed"
            );
        }

        return CompletableFuture.supplyAsync(() -> {

            try {
                return operation.execute(getConnection());

            } catch (SQLException exception) {

                throw new CompletionException(
                    "MySQL operation failed",
                    exception
                );
            }

        }, executor);
    }

    private Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {

        	try {
        	    Class.forName("org.mariadb.jdbc.Driver");

        	} catch (ClassNotFoundException exception) {

        	    throw new SQLException(
        	        "MariaDB JDBC driver could not be loaded.",
        	        exception
        	    );
        	}
        	
            try {

                String url =
                    "jdbc:mariadb://"
                    + config.getMySQLHost()
                    + ":"
                    + config.getMySQLPort()
                    + "/"
                    + config.getMySQLDatabase();

                Properties properties = new Properties();

                properties.setProperty(
                    "user",
                    config.getMySQLUsername()
                );

                properties.setProperty(
                    "password",
                    config.getMySQLPassword()
                );

                properties.setProperty(
                    "sslMode",
                    config.isMySQLUseSSL()
                        ? "verify-full"
                        : "disable"
                );

                connection = DriverManager.getConnection(
                    url,
                    properties
                );

                initializeDatabase();

            } catch (Exception exception) {

                if (connection != null) {

                    try {
                        connection.close();

                    } catch (SQLException ignored) {
                        // Ignore secondary close errors.
                    }

                    connection = null;
                }

                if (exception instanceof SQLException) {
                    throw (SQLException) exception;
                }

                throw new SQLException(
                    "Could not initialize MySQL database",
                    exception
                );
            }
        }

        return connection;
    }

    private void initializeDatabase() throws SQLException {

        String sql =
            "CREATE TABLE IF NOT EXISTS core_data ("
            + "data_key VARCHAR(191) "
            + "CHARACTER SET utf8mb4 "
            + "COLLATE utf8mb4_bin PRIMARY KEY, "
            + "data_value TEXT NOT NULL"
            + ") ENGINE=InnoDB";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void validateKey(String key) {

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Storage key cannot be null or empty"
            );
        }

        if (key.length() > 191) {
            throw new IllegalArgumentException(
                "Storage key cannot exceed 191 characters"
            );
        }
    }

    @Override
    public synchronized CompletableFuture<Void> close() {

        if (closeFuture != null) {
            return closeFuture;
        }

        closed = true;

        closeFuture = CompletableFuture.runAsync(() -> {

            if (connection != null) {

                try {
                    connection.close();
                    connection = null;

                } catch (SQLException exception) {

                    throw new CompletionException(
                        "Could not close MySQL connection",
                        exception
                    );
                }
            }

        }, executor).whenComplete((result, exception) -> {
            executor.shutdown();
        });

        return closeFuture;
    }

    @FunctionalInterface
    private interface MySQLOperation<T> {

        T execute(Connection connection) throws SQLException;
    }
}
