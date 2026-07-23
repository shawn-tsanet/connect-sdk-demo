package com.tsanet.api.connectapi.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsanet.api.OAuthClientCredentials;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Acquires bearer tokens from an OAuth 2.0 token endpoint using the
 * client-credentials grant. Deliberately not part of the generated Connect API
 * client: the token endpoint belongs to the identity provider (Microsoft
 * Entra), not to the Connect API surface.
 */
public class EntraClientCredentialsGateway {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EntraClientCredentialsGateway() {
        this(HttpClient.newHttpClient());
    }

    EntraClientCredentialsGateway(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public TokenResponse fetchToken(OAuthClientCredentials credentials) {
        String form = "grant_type=client_credentials"
            + "&client_id=" + encode(credentials.clientId())
            + "&client_secret=" + encode(credentials.clientSecret())
            + "&scope=" + encode(credentials.scope());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(credentials.tokenUrl()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while requesting OAuth token", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reach OAuth token endpoint " + credentials.tokenUrl(), e);
        }

        if (response.statusCode() != 200) {
            throw new IllegalStateException("OAuth token endpoint returned HTTP " + response.statusCode()
                + ": " + errorSummary(response.body()));
        }

        JsonNode body;
        try {
            body = objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new IllegalStateException("OAuth token endpoint returned unparseable JSON", e);
        }

        String accessToken = body.path("access_token").asText(null);
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("OAuth token response is missing access_token");
        }
        long expiresIn = body.path("expires_in").asLong(0);
        return new TokenResponse(accessToken, expiresIn);
    }

    /** Extracts the IdP error code and description without echoing the full body. */
    private String errorSummary(String responseBody) {
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            String error = node.path("error").asText("");
            String description = node.path("error_description").asText("");
            if (!error.isBlank()) {
                return description.isBlank() ? error : error + " - " + firstLine(description);
            }
        } catch (Exception ignored) {
            // fall through to generic summary
        }
        return "unrecognized error body";
    }

    private String firstLine(String text) {
        int newline = text.indexOf('\n');
        return newline >= 0 ? text.substring(0, newline) : text;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** A minted bearer token and its advertised lifetime ({@code 0} if the IdP omitted it). */
    public record TokenResponse(String accessToken, long expiresInSeconds) {
    }
}
