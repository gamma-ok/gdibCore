package me.gdib.core.common.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.bukkit.plugin.java.JavaPlugin;

public final class SQLiteProvider implements StorageProvider {

    private final Path databasePath;
    private final ExecutorService executor;

    private Connection connection;

    private volatile boolean closed;

    private CompletableFuture<Void> closeFuture;

    public SQLiteProvider(JavaPlugin plugin, String fileName) {

        Objects.requireNonNull(plugin, "Plugin cannot be null");

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "SQLite file name cannot be empty"
            );
        }

        Path dataFolder = plugin.getDataFolder()
            .toPath()
            .toAbsolutePath()
            .normalize();

        Path requestedPath = Path.of(fileName);

        if (requestedPath.isAbsolute()) {
            throw new IllegalArgumentException(
                "SQLite file path must be relative"
            );
        }

        this.databasePath = dataFolder.resolve(requestedPath)
            .normalize();

        if (!databasePath.startsWith(dataFolder)
                || databasePath.equals(dataFolder)) {

            throw new IllegalArgumentException(
                "Invalid SQLite database path"
            );
        }

        ThreadFactory threadFactory = runnable -> {

            Thread thread = new Thread(
                runnable,
                "gdibCore-SQLite"
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
                + "ON CONFLICT(data_key) "
                + "DO UPDATE SET data_value = excluded.data_value";

            try (PreparedStatement statement =
                    conn.prepareStatement(sql)) {

                statement.setString(1, key);
                statement.setString(2, value);

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
            SQLiteOperation<T> operation) {

        if (closed) {
            throw new IllegalStateException(
                "SQLite provider is already closed"
            );
        }

        return CompletableFuture.supplyAsync(() -> {

            try {
                return operation.execute(getConnection());

            } catch (SQLException exception) {

                throw new CompletionException(
                    "SQLite operation failed",
                    exception
                );
            }

        }, executor);
    }

    private Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {

            try {

                Path parent = databasePath.getParent();

                if (parent != null) {
                    Files.createDirectories(parent);
                }

                connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + databasePath
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
                    "Could not initialize SQLite database",
                    exception
                );
            }
        }

        return connection;
    }

    private void initializeDatabase() throws SQLException {

        String sql =
            "CREATE TABLE IF NOT EXISTS core_data ("
            + "data_key TEXT PRIMARY KEY, "
            + "data_value TEXT NOT NULL"
            + ")";

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
                        "Could not close SQLite connection",
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
    private interface SQLiteOperation<T> {

        T execute(Connection connection) throws SQLException;
    }
}
