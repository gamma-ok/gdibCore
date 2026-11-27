package me.gdib.core.modules.tab.skin;

public final class MineSkinApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public MineSkinApiException(
            int statusCode,
            String responseBody
    ) {
        super(
                "MineSkin API request failed. HTTP status: "
                + statusCode
        );

        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
