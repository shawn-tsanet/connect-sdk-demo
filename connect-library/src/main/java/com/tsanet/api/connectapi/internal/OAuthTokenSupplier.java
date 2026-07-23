package com.tsanet.api.connectapi.internal;

import com.tsanet.api.OAuthClientCredentials;
import java.util.function.Supplier;

/**
 * Bearer-token supplier for the generated {@code ApiClient}. In password mode
 * it simply returns the stored login JWT (unchanged behavior). In OAuth
 * client-credentials mode it transparently re-mints the token shortly before
 * expiry, so long-running sessions never present a stale bearer.
 */
public class OAuthTokenSupplier implements Supplier<String> {

    /** Re-mint this long before the advertised expiry to absorb clock skew and in-flight latency. */
    static final long REFRESH_MARGIN_MILLIS = 60_000;

    /**
     * Assumed lifetime when the IdP omits {@code expires_in}. Without a
     * fallback the token would never be considered stale and the session
     * would 401 permanently once it actually expires.
     */
    static final long DEFAULT_LIFETIME_SECONDS = 3300;

    private final ConnectApiSessionStore sessionStore;
    private final EntraClientCredentialsGateway oauthGateway;

    public OAuthTokenSupplier(ConnectApiSessionStore sessionStore, EntraClientCredentialsGateway oauthGateway) {
        this.sessionStore = sessionStore;
        this.oauthGateway = oauthGateway;
    }

    @Override
    public String get() {
        if (sessionStore.needsOAuthRefresh(REFRESH_MARGIN_MILLIS)) {
            refresh();
        }
        return sessionStore.getBearerToken().orElse(null);
    }

    private synchronized void refresh() {
        if (!sessionStore.needsOAuthRefresh(REFRESH_MARGIN_MILLIS)) {
            return; // another thread refreshed while we waited on the lock
        }
        OAuthClientCredentials credentials = sessionStore.getOAuthConfig().orElse(null);
        if (credentials == null) {
            return;
        }
        EntraClientCredentialsGateway.TokenResponse token = oauthGateway.fetchToken(credentials);
        sessionStore.saveOAuth(
            credentials.clientId(),
            token.accessToken(),
            expiresAt(token.expiresInSeconds()),
            credentials
        );
    }

    public static long expiresAt(long expiresInSeconds) {
        long effective = expiresInSeconds > 0 ? expiresInSeconds : DEFAULT_LIFETIME_SECONDS;
        return System.currentTimeMillis() + effective * 1000;
    }
}
