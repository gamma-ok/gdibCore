package me.gdib.core.modules.tab.skin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import me.gdib.core.modules.tab.layout.TabSkinTextureManager;
import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

public final class MineSkinService {

    private final MineSkinApiClient apiClient;
    private final MineSkinResponseParser responseParser;
    private final TabSkinTextureManager textureManager;
    private final MineSkinConfig config;

    private final ConcurrentMap<String, CompletableFuture<TabSkinTexture>>
            pendingRequests;

    private final AtomicBoolean closed;

    public MineSkinService(
            MineSkinApiClient apiClient,
            MineSkinResponseParser responseParser,
            TabSkinTextureManager textureManager,
            MineSkinConfig config
    ) {
        this.apiClient = Objects.requireNonNull(
                apiClient,
                "MineSkinApiClient cannot be null"
        );

        this.responseParser = Objects.requireNonNull(
                responseParser,
                "MineSkinResponseParser cannot be null"
        );

        this.textureManager = Objects.requireNonNull(
                textureManager,
                "TabSkinTextureManager cannot be null"
        );

        this.config = Objects.requireNonNull(
                config,
                "MineSkinConfig cannot be null"
        );

        if (!config.isEnabled()) {
            throw new IllegalArgumentException(
                    "MineSkin integration is disabled."
            );
        }

        this.pendingRequests = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean(false);
    }

    public CompletableFuture<TabSkinTexture> requestTexture(
            String identifier,
            String skinUrl
    ) {
        ensureOpen();

        String key = normalizeIdentifier(identifier);

        if (skinUrl == null || skinUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Skin URL cannot be empty."
            );
        }

        TabSkinTexture cached = textureManager.get(key);

        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Reserve the request before starting the HTTP operation.
        CompletableFuture<TabSkinTexture> request =
                new CompletableFuture<>();

        CompletableFuture<TabSkinTexture> existing =
                pendingRequests.putIfAbsent(key, request);

        if (existing != null) {
            return existing;
        }

        // Start generation only after successfully reserving the key.
        generateTexture(key, skinUrl.trim())
                .whenComplete((texture, error) -> {

                    pendingRequests.remove(key, request);

                    if (error != null) {
                        request.completeExceptionally(error);
                    } else {
                        request.complete(texture);
                    }
                });

        return request;
    }

    private CompletableFuture<TabSkinTexture> generateTexture(
            String identifier,
            String skinUrl
    ) {
        return apiClient.submitSkinGeneration(
                skinUrl,
                config.getApiKey()
        ).thenCompose(response -> {

            ensureOpen();

            MineSkinResponseParser.JobResponse job =
                    responseParser.parse(response);

            if (job.status()
                    == MineSkinResponseParser.JobStatus.COMPLETED) {

                return CompletableFuture.completedFuture(
                        storeTexture(identifier, job.texture())
                );
            }

            if (job.status()
                    == MineSkinResponseParser.JobStatus.FAILED
                    || job.status()
                    == MineSkinResponseParser.JobStatus.UNKNOWN) {

                return CompletableFuture.failedFuture(
                        new IllegalStateException(
                                "MineSkin generation failed or returned an unknown status."
                        )
                );
            }

            return pollJob(identifier, job.jobId(), 0);
        });
    }

    private CompletableFuture<TabSkinTexture> pollJob(
            String identifier,
            String jobId,
            int attempt
    ) {
        ensureOpen();

        if (attempt >= config.getMaxPollAttempts()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "MineSkin generation timed out."
                    )
            );
        }

        return CompletableFuture.runAsync(
                () -> {
                    // Delayed execution without blocking a thread.
                },
                CompletableFuture.delayedExecutor(
                        config.getPollIntervalSeconds(),
                        TimeUnit.SECONDS
                )
        ).thenCompose(ignored -> {

            ensureOpen();

            return apiClient.getJobStatus(
                    jobId,
                    config.getApiKey()
            );

        }).thenCompose(response -> {

            ensureOpen();

            MineSkinResponseParser.JobResponse job =
                    responseParser.parse(response);

            switch (job.status()) {

                case COMPLETED:
                    return CompletableFuture.completedFuture(
                            storeTexture(identifier, job.texture())
                    );

                case WAITING:
                case ACTIVE:
                    return pollJob(
                            identifier,
                            jobId,
                            attempt + 1
                    );

                case FAILED:
                    return CompletableFuture.failedFuture(
                            new IllegalStateException(
                                    "MineSkin generation failed."
                            )
                    );

                default:
                    return CompletableFuture.failedFuture(
                            new IllegalStateException(
                                    "Unknown MineSkin job status."
                            )
                    );
            }
        });
    }

    private TabSkinTexture storeTexture(
            String identifier,
            TabSkinTexture texture
    ) {
        ensureOpen();

        Objects.requireNonNull(
                texture,
                "Generated texture cannot be null"
        );

        textureManager.registerOrReplace(
                identifier,
                texture
        );

        return texture;
    }

    private String normalizeIdentifier(String identifier) {
        Objects.requireNonNull(
                identifier,
                "Texture identifier cannot be null"
        );

        String key = identifier.trim().toLowerCase(
                java.util.Locale.ROOT
        );

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                    "Texture identifier cannot be empty."
            );
        }

        return key;
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "MineSkin service is closed."
            );
        }
    }

    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    public boolean isClosed() {
        return closed.get();
    }

    public void shutdown() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        for (CompletableFuture<TabSkinTexture> future :
                pendingRequests.values()) {

            future.cancel(true);
        }

        pendingRequests.clear();
        apiClient.shutdown();
    }
}
