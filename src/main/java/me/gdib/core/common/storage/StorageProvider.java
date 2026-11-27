package me.gdib.core.common.storage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the contract for gdibCore storage providers.
 *
 * Implementations can use different database engines,
 * such as SQLite or MySQL.
 */
public interface StorageProvider {

    /**
     * Saves or updates a value associated with a key.
     */
    CompletableFuture<Void> save(String key, String value);

    /**
     * Loads the value associated with a key.
     */
    CompletableFuture<Optional<String>> load(String key);

    /**
     * Checks whether a key exists.
     */
    CompletableFuture<Boolean> exists(String key);

    /**
     * Deletes the value associated with a key.
     */
    CompletableFuture<Void> delete(String key);

    /**
     * Closes the provider and releases its resources.
     */
    CompletableFuture<Void> close();
}
