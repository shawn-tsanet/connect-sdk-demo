package com.tsanet.api;

/**
 * OAuth 2.0 client-credentials configuration for authenticating against the
 * Connect API via an identity provider (Microsoft Entra in production).
 *
 * <p>The token endpoint and scope differ per environment (audiences are
 * environment-specific), so both are supplied as configuration rather than
 * constants. Note the Entra scope for the Connect API is the bare
 * {@code {audience}/.default} form.
 */
public record OAuthClientCredentials(
    String tokenUrl,
    String clientId,
    String clientSecret,
    String scope
) {
    public OAuthClientCredentials {
        if (tokenUrl == null || tokenUrl.isBlank()) {
            throw new IllegalArgumentException("tokenUrl is required");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId is required");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("clientSecret is required");
        }
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("scope is required");
        }
    }

    public static OAuthClientCredentials of(String tokenUrl, String clientId, String clientSecret, String scope) {
        return new OAuthClientCredentials(tokenUrl, clientId, clientSecret, scope);
    }
}
