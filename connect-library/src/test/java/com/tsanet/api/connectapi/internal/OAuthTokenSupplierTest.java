package com.tsanet.api.connectapi.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.tsanet.api.OAuthClientCredentials;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OAuthTokenSupplierTest {

    private static final OAuthClientCredentials CREDS =
        OAuthClientCredentials.of("http://localhost/token", "client-1", "secret-1", "aud/.default");

    /** Gateway stub that counts mints and returns sequenced tokens. */
    private static final class StubGateway extends EntraClientCredentialsGateway {
        final AtomicInteger mints = new AtomicInteger();
        volatile long expiresInSeconds = 3600;

        @Override
        public TokenResponse fetchToken(OAuthClientCredentials credentials) {
            return new TokenResponse("tok-" + mints.incrementAndGet(), expiresInSeconds);
        }
    }

    @Test
    void passwordModePassesTheStoredTokenThroughWithoutMinting() {
        var store = new ConnectApiSessionStore();
        var gateway = new StubGateway();
        store.save("user@example.com", "login-jwt");

        var supplier = new OAuthTokenSupplier(store, gateway);

        assertThat(supplier.get()).isEqualTo("login-jwt");
        assertThat(gateway.mints.get()).isZero();
    }

    @Test
    void oauthModeReturnsAFreshTokenWhileUnexpired() {
        var store = new ConnectApiSessionStore();
        var gateway = new StubGateway();
        store.saveOAuth("client-1", "tok-0", System.currentTimeMillis() + 3_600_000, CREDS);

        var supplier = new OAuthTokenSupplier(store, gateway);

        assertThat(supplier.get()).isEqualTo("tok-0");
        assertThat(gateway.mints.get()).isZero();
    }

    @Test
    void oauthModeRemintsInsideTheExpiryMargin() {
        var store = new ConnectApiSessionStore();
        var gateway = new StubGateway();
        // expires 1s from now: inside the 60s refresh margin
        store.saveOAuth("client-1", "tok-stale", System.currentTimeMillis() + 1_000, CREDS);

        var supplier = new OAuthTokenSupplier(store, gateway);

        assertThat(supplier.get()).isEqualTo("tok-1");
        assertThat(gateway.mints.get()).isEqualTo(1);
        // and the re-minted token is cached, not re-minted per call
        assertThat(supplier.get()).isEqualTo("tok-1");
        assertThat(gateway.mints.get()).isEqualTo(1);
    }

    @Test
    void expiresAtFallsBackToADefaultLifetimeWhenExpiresInIsAbsent() {
        long before = System.currentTimeMillis();
        long expiry = OAuthTokenSupplier.expiresAt(0);
        // absent expires_in must still yield a real future expiry, or the
        // token would never be considered stale (QA finding)
        assertThat(expiry).isGreaterThanOrEqualTo(
            before + (OAuthTokenSupplier.DEFAULT_LIFETIME_SECONDS - 1) * 1000);
    }

    @Test
    void clearedSessionSuppliesNullAndDoesNotMint() {
        var store = new ConnectApiSessionStore();
        var gateway = new StubGateway();
        store.saveOAuth("client-1", "tok-0", System.currentTimeMillis() + 3_600_000, CREDS);
        store.clear();

        var supplier = new OAuthTokenSupplier(store, gateway);

        assertThat(supplier.get()).isNull();
        assertThat(gateway.mints.get()).isZero();
    }
}
