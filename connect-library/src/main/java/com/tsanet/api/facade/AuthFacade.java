package com.tsanet.api.facade;

import com.tsanet.api.OAuthClientCredentials;
import java.util.Optional;

public interface AuthFacade {
    String login(String username, String password);

    /**
     * Authenticates via the OAuth 2.0 client-credentials grant. The session
     * transparently re-mints the token before expiry, so callers never need to
     * re-authenticate for the life of the session.
     */
    String loginWithClientCredentials(OAuthClientCredentials credentials);

    String loginWithConfiguredCredentials();

    boolean isAuthorized();

    Optional<String> currentUsername();

    Optional<String> currentBearerToken();

    void logout();
}
