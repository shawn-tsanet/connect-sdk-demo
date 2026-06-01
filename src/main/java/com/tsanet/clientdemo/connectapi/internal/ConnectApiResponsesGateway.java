package com.tsanet.clientdemo.connectapi.internal;

import static com.tsanet.clientdemo.connectapi.internal.OpenApiMapping.dateTime;
import static com.tsanet.clientdemo.connectapi.internal.OpenApiMapping.enumValue;

import com.tsanet.clientdemo.connectapi.dto.CaseResponseDto;
import com.tsanet.clientdemo.generated.api.CollaborationRequestsApi;
import com.tsanet.clientdemo.generated.model.CaseResponseDTO;
import com.tsanet.clientdemo.generated.model.CollaborationRequestStatusDTO;
import com.tsanet.clientdemo.storage.CaseResponseStorageService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConnectApiResponsesGateway {
    private final CollaborationRequestsApi collaborationRequestsApi;
    private final ConnectApiSessionStore sessionStore;
    private final CaseResponseStorageService storageService;

    public ConnectApiResponsesGateway(
        CollaborationRequestsApi collaborationRequestsApi,
        ConnectApiSessionStore sessionStore,
        CaseResponseStorageService storageService
    ) {
        this.collaborationRequestsApi = collaborationRequestsApi;
        this.sessionStore = sessionStore;
        this.storageService = storageService;
    }

    public List<CaseResponseDto> getResponses(String caseToken) {
        requireLogin();

        CollaborationRequestStatusDTO body = collaborationRequestsApi.getCollaborationRequestByToken(caseToken, false);
        if (body == null || body.getCaseResponses() == null) {
            return Collections.emptyList();
        }
        List<CaseResponseDto> responses = body.getCaseResponses().stream().map(response -> toDto(response, caseToken)).toList();
        storageService.storeFetched(responses);
        return responses;
    }

    private CaseResponseDto toDto(CaseResponseDTO dto, String caseToken) {
        return new CaseResponseDto(
            dto.getId(),
            caseToken,
            enumValue(dto.getType()),
            dto.getCaseNumber(),
            dto.getEngineerName(),
            dto.getEngineerPhone(),
            dto.getEngineerEmail(),
            dto.getNextSteps(),
            dateTime(dto.getCreatedAt())
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
