package me.gdib.core.modules.tab.skin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class MineSkinApiClient {

    private static final String API_URL =
            "https://api.mineskin.org/v2";

    private static final String USER_AGENT =
            "gdibCore/0.0.1";

    private final HttpClient httpClient;

    public MineSkinApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<String> submitSkinGeneration(
            String skinUrl,
            String apiKey
    ) {
        Objects.requireNonNull(
                skinUrl,
                "Skin URL cannot be null"
        );

        String normalizedUrl = skinUrl.trim();

        if (normalizedUrl.isEmpty()) {
            throw new IllegalArgumentException(
                    "Skin URL cannot be empty."
            );
        }

        String requestBody = "{"
                + "\"url\":\"" + escapeJson(normalizedUrl) + "\","
                + "\"variant\":\"unknown\","
                + "\"visibility\":\"unlisted\""
                + "}";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/queue"))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        addAuthorization(builder, apiKey);

        return sendRequest(builder.build());
    }

    public CompletableFuture<String> getJobStatus(
            String jobId,
            String apiKey
    ) {
        Objects.requireNonNull(
                jobId,
                "MineSkin job ID cannot be null"
        );

        String normalizedJobId = jobId.trim();

        if (normalizedJobId.isEmpty()) {
            throw new IllegalArgumentException(
                    "MineSkin job ID cannot be empty."
            );
        }

        String encodedJobId = java.net.URLEncoder
                .encode(
                        normalizedJobId,
                        java.nio.charset.StandardCharsets.UTF_8
                )
                .replace("+", "%20");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(
                        API_URL + "/queue/" + encodedJobId
                ))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET();

        addAuthorization(builder, apiKey);

        return sendRequest(builder.build());
    }

    private void addAuthorization(
            HttpRequest.Builder builder,
            String apiKey
    ) {
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header(
                    "Authorization",
                    "Bearer " + apiKey.trim()
            );
        }
    }

    private CompletableFuture<String> sendRequest(
            HttpRequest request
    ) {
        return httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
        ).thenApply(response -> {

            int statusCode = response.statusCode();

            if (statusCode < 200 || statusCode >= 300) {
                throw new MineSkinApiException(
                        statusCode,
                        response.body()
                );
            }

            return response.body();
        });
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void shutdown() {
        httpClient.close();
    }
}
