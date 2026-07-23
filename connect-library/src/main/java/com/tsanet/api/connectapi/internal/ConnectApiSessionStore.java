package com.tsanet.api.connectapi.internal;

import com.tsanet.api.OAuthClientCredentials;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectApiSessionStore {

    /**
     * Immutable auth snapshot swapped atomically, so readers never observe a
     * mix of one login's token with another's expiry or mode.
     *
     * <p>{@code expiresAtEpochMillis} of {@code 0} means no known expiry
     * (password mode). {@code oauthConfig} is present only in OAuth
     * client-credentials mode and enables transparent re-mint on expiry.
     */
    private record AuthState(
        String username,
        String bearerToken,
        long expiresAtEpochMillis,
        OAuthClientCredentials oauthConfig
    ) {
        static final AuthState EMPTY = new AuthState(null, null, 0, null);
    }

    private final AtomicReference<AuthState> state = new AtomicReference<>(AuthState.EMPTY);

    public void save(String username, String bearerToken) {
        state.set(new AuthState(username, bearerToken, 0, null));
    }

    public void saveOAuth(String principal, String bearerToken, long expiresAtEpochMillis,
                          OAuthClientCredentials oauthConfig) {
        state.set(new AuthState(principal, bearerToken, expiresAtEpochMillis, oauthConfig));
    }

    public Optional<String> getBearerToken() {
        return Optional.ofNullable(state.get().bearerToken());
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(state.get().username());
    }

    public Optional<OAuthClientCredentials> getOAuthConfig() {
        return Optional.ofNullable(state.get().oauthConfig());
    }

    /**
     * True when the session is in OAuth mode and the token is missing or
     * inside the refresh margin of its expiry. Always false in password mode.
     */
    public boolean needsOAuthRefresh(long marginMillis) {
        AuthState current = state.get();
        if (current.oauthConfig() == null) {
            return false;
        }
        if (current.bearerToken() == null || current.bearerToken().isBlank()) {
            return true;
        }
        return current.expiresAtEpochMillis() > 0
            && System.currentTimeMillis() >= current.expiresAtEpochMillis() - marginMillis;
    }

    public boolean isAuthorized() {
        String token = state.get().bearerToken();
        return token != null && !token.isBlank();
    }

    public void clear() {
        state.set(AuthState.EMPTY);
    }
}
