package com.tsanet.demo.web;

import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.connectapi.dto.CollaborationRequestStatusDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollaborationRequestsController {

    private final TsaNetApiSession session;

    public CollaborationRequestsController(TsaNetApiSession session) {
        this.session = session;
    }

    private void ensureAuthenticated() {
        if (!session.auth().isAuthorized()) {
            session.auth().loginWithConfiguredCredentials();
        }
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
