package me.gdib.core.common.storage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {

    CompletableFuture<Void> save(String key, String value);

    CompletableFuture<Optional<String>> load(String key);

    CompletableFuture<Boolean> exists(String key);

    CompletableFuture<Void> delete(String key);

    CompletableFuture<Void> close();
}
