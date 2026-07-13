package com.tsanet.demo.web;

import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.connectapi.dto.CollaborationRequestStatusDto;
import com.tsanet.demo.config.CredentialsStore;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CollaborationRequestsController {

    private final TsaNetApiSession session;
    private final CredentialsStore credentialsStore;

    public CollaborationRequestsController(TsaNetApiSession session, CredentialsStore credentialsStore) {
        this.session = session;
        this.credentialsStore = credentialsStore;
    }

    private void ensureAuthenticated() {
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

    @GetMapping("/api/requests")
    public List<CollaborationRequestStatusDto> listRequests() {
        ensureAuthenticated();
        return session.collaborationRequests().listRequests();
    }

    @PostMapping("/api/requests")
    public CollaborationRequestStatusDto createRequest(@RequestBody CreateRequestBody body) {
        ensureAuthenticated();
        return session.collaborationRequests().createRequest(
            body.receiverCompanyId(),
            body.caseNumber(),
            body.summary(),
            body.description()
        );
    }

    public record CreateRequestBody(
        long receiverCompanyId,
        String caseNumber,
        String summary,
        String description
    ) {
    }
}
