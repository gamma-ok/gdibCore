package me.gdib.core.modules.tab.skin;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import me.gdib.core.modules.tab.layout.model.TabSkinTexture;

public final class MineSkinResponseParser {

    public enum JobStatus {
        WAITING,
        ACTIVE,
        COMPLETED,
        FAILED,
        UNKNOWN
    }

    public record JobResponse(
            String jobId,
            JobStatus status,
            TabSkinTexture texture
    ) {
    }

    public JobResponse parse(String json) {
        JsonObject root = parseJson(json);

        JsonObject job = getObject(root, "job");

        if (job == null) {
            throw new IllegalArgumentException(
                    "MineSkin response does not contain a job object."
            );
        }

        String jobId = getString(job, "id");

        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException(
                    "MineSkin response does not contain a job ID."
            );
        }

        JobStatus status = parseStatus(
                getString(job, "status")
        );

        TabSkinTexture texture = null;

        if (status == JobStatus.COMPLETED) {
            texture = parseTexture(root);

            if (texture == null) {
                throw new IllegalArgumentException(
                        "Completed MineSkin job does not contain texture data."
                );
            }
        }

        if (status == JobStatus.FAILED) {
            throw new IllegalStateException(
                    "MineSkin generation failed: " + getErrors(root)
            );
        }

        return new JobResponse(jobId, status, texture);
    }

    public String parseJobId(String json) {
        JsonObject root = parseJson(json);
        JsonObject job = getObject(root, "job");

        if (job == null) {
            throw new IllegalArgumentException(
                    "MineSkin response does not contain a job object."
            );
        }

        String jobId = getString(job, "id");

        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException(
                    "MineSkin response does not contain a job ID."
            );
        }

        return jobId;
    }

    public JobStatus parseStatus(String status) {
        if (status == null) {
            return JobStatus.UNKNOWN;
        }

        return switch (status.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "waiting" -> JobStatus.WAITING;
            case "active" -> JobStatus.ACTIVE;
            case "completed" -> JobStatus.COMPLETED;
            case "failed" -> JobStatus.FAILED;
            default -> JobStatus.UNKNOWN;
        };
    }

    public TabSkinTexture parseTexture(String json) {
        return parseTexture(parseJson(json));
    }

    private TabSkinTexture parseTexture(JsonObject root) {
        JsonObject skin = getObject(root, "skin");

        if (skin == null) {
            return null;
        }

        JsonObject texture = getObject(skin, "texture");

        if (texture == null) {
            return null;
        }

        JsonObject data = getObject(texture, "data");

        if (data == null) {
            return null;
        }

        String value = getString(data, "value");
        String signature = getString(data, "signature");

        if (value == null || value.isBlank()) {
            return null;
        }

        return new TabSkinTexture(
                value,
                signature,
                TabSkinTexture.Source.MINESKIN
        );
    }

    private JsonObject parseJson(String json) {
        Objects.requireNonNull(
                json,
                "JSON response cannot be null"
        );

        try {
            JsonElement element = JsonParser.parseString(json);

            if (!element.isJsonObject()) {
                throw new IllegalArgumentException(
                        "MineSkin response must be a JSON object."
                );
            }

            return element.getAsJsonObject();

        } catch (JsonParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid JSON response from MineSkin.",
                    exception
            );
        }
    }

    private JsonObject getObject(
            JsonObject parent,
            String property
    ) {
        if (parent == null || !parent.has(property)) {
            return null;
        }

        JsonElement element = parent.get(property);

        if (element == null || !element.isJsonObject()) {
            return null;
        }

        return element.getAsJsonObject();
    }

    private String getString(
            JsonObject parent,
            String property
    ) {
        if (parent == null || !parent.has(property)) {
            return null;
        }

        JsonElement element = parent.get(property);

        if (element == null || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()) {
            return null;
        }

        return element.getAsString();
    }

    private String getErrors(JsonObject root) {
        if (!root.has("errors") || !root.get("errors").isJsonArray()) {
            return "No additional error information.";
        }

        return root.get("errors").getAsJsonArray().toString();
    }
}
