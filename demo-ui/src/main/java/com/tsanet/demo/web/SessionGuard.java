package com.tsanet.demo.web;

import com.tsanet.api.TsaNetApiSession;
import com.tsanet.demo.config.CredentialsStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Ensures the shared SDK session is logged in before facade calls,
 * using the credentials persisted via the settings UI.
 */
@Component
public class SessionGuard {

    private final TsaNetApiSession session;
    private final CredentialsStore credentialsStore;

    public SessionGuard(TsaNetApiSession session, CredentialsStore credentialsStore) {
        this.session = session;
        this.credentialsStore = credentialsStore;
    }

    public void ensureAuthenticated() {
        if (session.auth().isAuthorized()) {
            return;
        }
        CredentialsStore.Credentials credentials = credentialsStore.load()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.PRECONDITION_REQUIRED,
                "BETA credentials not configured - set them under Settings first"
            ));
        session.auth().login(credentials.username(), credentials.password());
    }
}
