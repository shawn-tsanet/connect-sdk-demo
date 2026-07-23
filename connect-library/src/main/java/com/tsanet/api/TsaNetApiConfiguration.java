package com.tsanet.api;

public record TsaNetApiConfiguration(
    String apiBaseUrl,
    String sqlitePath,
    String username,
    String password,
    OAuthClientCredentials oauth
) {
    public TsaNetApiConfiguration {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            throw new IllegalArgumentException("apiBaseUrl is required");
        }
        if (sqlitePath == null || sqlitePath.isBlank()) {
            throw new IllegalArgumentException("sqlitePath is required");
        }
    }

    public static TsaNetApiConfiguration of(String apiBaseUrl, String sqlitePath, String username, String password) {
        return new TsaNetApiConfiguration(apiBaseUrl, sqlitePath, username, password, null);
    }

    /** OAuth client-credentials mode: no static username/password, tokens minted from the IdP. */
    public static TsaNetApiConfiguration ofOAuth(String apiBaseUrl, String sqlitePath, OAuthClientCredentials oauth) {
        if (oauth == null) {
            throw new IllegalArgumentException("oauth configuration is required");
        }
        return new TsaNetApiConfiguration(apiBaseUrl, sqlitePath, null, null, oauth);
    }
}
