package me.gdib.core.common.storage;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class StorageManager {

    private final StorageProvider provider;

    private volatile boolean closed;

    public StorageManager(StorageProvider provider) {
        this.provider = Objects.requireNonNull(
            provider,
            "StorageProvider cannot be null"
        );

        this.closed = false;
    }

    public CompletableFuture<Void> save(
            String key,
            String value) {

        ensureOpen();

        return Objects.requireNonNull(
            provider.save(key, value),
            "Storage provider returned a null future"
        );
    }

    public CompletableFuture<Optional<String>> load(String key) {
        ensureOpen();

        return Objects.requireNonNull(
            provider.load(key),
            "Storage provider returned a null future"
        );
    }

    public CompletableFuture<Boolean> exists(String key) {
        ensureOpen();

        return Objects.requireNonNull(
            provider.exists(key),
            "Storage provider returned a null future"
        );
    }

    public CompletableFuture<Void> delete(String key) {
        ensureOpen();

        return Objects.requireNonNull(
            provider.delete(key),
            "Storage provider returned a null future"
        );
    }

    public CompletableFuture<Void> shutdown() {
        if (closed) {
            return CompletableFuture.completedFuture(null);
        }

        closed = true;

        return Objects.requireNonNull(
            provider.close(),
            "Storage provider returned a null future"
        );
    }

    public boolean isClosed() {
        return closed;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException(
                "Storage manager is already closed"
            );
        }
    }
}
