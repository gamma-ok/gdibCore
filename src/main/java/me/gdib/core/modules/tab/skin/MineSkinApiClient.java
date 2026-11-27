package me.gdib.core.modules.tab.skin;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

public final class MineSkinApiClient {

	private static final String API_URL = "https://api.mineskin.org/v2";

	private static final String USER_AGENT = "gdibCore/0.0.1";

	private final HttpClient httpClient;

	public MineSkinApiClient() {

		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	public CompletableFuture<String> submitSkinGeneration(String skinUrl, String apiKey) {

		Objects.requireNonNull(skinUrl, "Skin URL cannot be null");

		String normalizedUrl = skinUrl.trim();

		if (normalizedUrl.isEmpty()) {
			throw new IllegalArgumentException("Skin URL cannot be empty.");
		}

		// Build request JSON safely using Gson.
		JsonObject requestBody = new JsonObject();

		requestBody.addProperty("url", normalizedUrl);
		requestBody.addProperty("variant", "unknown");
		requestBody.addProperty("visibility", "unlisted");

		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(API_URL + "/queue"))
				.timeout(Duration.ofSeconds(30)).header("User-Agent", USER_AGENT)
				.header("Content-Type", "application/json").header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8));

		addAuthorization(builder, apiKey);

		return sendRequest(builder.build());
	}

	public CompletableFuture<String> getJobStatus(String jobId, String apiKey) {

		Objects.requireNonNull(jobId, "MineSkin job ID cannot be null");

		String normalizedJobId = jobId.trim();

		if (normalizedJobId.isEmpty()) {
			throw new IllegalArgumentException("MineSkin job ID cannot be empty.");
		}

		String encodedJobId = URLEncoder.encode(normalizedJobId, StandardCharsets.UTF_8).replace("+", "%20");

		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(API_URL + "/queue/" + encodedJobId))
				.timeout(Duration.ofSeconds(30)).header("User-Agent", USER_AGENT).header("Accept", "application/json")
				.GET();

		addAuthorization(builder, apiKey);

		return sendRequest(builder.build());
	}

	private void addAuthorization(HttpRequest.Builder builder, String apiKey) {

		if (apiKey != null && !apiKey.isBlank()) {

			builder.header("Authorization", "Bearer " + apiKey.trim());
		}
	}

	private CompletableFuture<String> sendRequest(HttpRequest request) {

		return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
				.thenApply(response -> {

					int statusCode = response.statusCode();

					if (statusCode < 200 || statusCode >= 300) {

						throw new MineSkinApiException(statusCode, response.body());
					}

					return response.body();
				});
	}

	public void shutdown() {
		httpClient.close();
	}
}
