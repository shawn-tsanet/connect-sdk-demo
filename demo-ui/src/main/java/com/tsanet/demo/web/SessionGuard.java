package com.tsanet.demo.web;

import com.tsanet.api.OAuthClientCredentials;
import com.tsanet.api.TsaNetApiSession;
import com.tsanet.demo.config.CredentialsStore;
import com.tsanet.demo.config.DemoProperties;
import com.tsanet.demo.config.EnvironmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves the active environment's SDK session and ensures it is logged in
 * before facade calls, using that environment's persisted credentials.
 */
@Component
public class SessionGuard {

    private final EnvironmentService environments;

    public SessionGuard(EnvironmentService environments) {
        this.environments = environments;
    }

    /** Authenticated session for the active environment. */
    public TsaNetApiSession session() {
        TsaNetApiSession session = environments.currentSession();
        if (session.auth().isAuthorized()) {
            return session;
        }
        CredentialsStore.Credentials credentials = environments.currentCredentials().load()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.PRECONDITION_REQUIRED,
                environments.activeDefinition().label()
                    + " credentials not configured - set them under Settings first"
            ));
        if (credentials.isOAuth()) {
            DemoProperties.EnvironmentDef def = environments.activeDefinition();
            if (!def.oauthAvailable()) {
                throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_REQUIRED,
                    def.label() + " has no Entra tenant/audience configured - OAuth mode unavailable"
                );
            }
            session.auth().loginWithClientCredentials(OAuthClientCredentials.of(
                def.oauthTokenUrl(),
                credentials.clientId(),
                credentials.clientSecret(),
                def.oauthScope()
            ));
        } else {
            session.auth().login(credentials.username(), credentials.password());
        }
        return session;
    }
}
