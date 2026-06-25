package com.tsanet.api.connectapi.internal;

import static com.tsanet.api.connectapi.internal.OpenApiMapping.dateTime;
import static com.tsanet.api.connectapi.internal.OpenApiMapping.enumValue;

import com.tsanet.api.connectapi.dto.CaseResponseDto;
import com.tsanet.api.connectapi.dto.CollaborationRequestStatusDto;
import com.tsanet.api.generated.api.CaseResponsesApi;
import com.tsanet.api.generated.api.CollaborationRequestsApi;
import com.tsanet.api.generated.model.CaseApprovalDTO;
import com.tsanet.api.generated.model.CaseResponseDTO;
import com.tsanet.api.generated.model.CollaborationRequestStatusDTO;
import com.tsanet.api.storage.CaseResponseStorageService;
import com.tsanet.api.storage.CollaborationRequestStorageService;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public class ConnectApiResponsesGateway {
    private final CollaborationRequestsApi collaborationRequestsApi;
    private final CaseResponsesApi caseResponsesApi;
    private final ConnectApiSessionStore sessionStore;
    private final CaseResponseStorageService storageService;
    private final CollaborationRequestStorageService collaborationStorageService;

    public ConnectApiResponsesGateway(
        CollaborationRequestsApi collaborationRequestsApi,
        CaseResponsesApi caseResponsesApi,
        ConnectApiSessionStore sessionStore,
        CaseResponseStorageService storageService,
        CollaborationRequestStorageService collaborationStorageService
    ) {
        this.collaborationRequestsApi = collaborationRequestsApi;
        this.caseResponsesApi = caseResponsesApi;
        this.sessionStore = sessionStore;
        this.storageService = storageService;
        this.collaborationStorageService = collaborationStorageService;
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

    public CollaborationRequestStatusDto approveCollaborationRequest(
        String caseToken,
        String caseNumber,
        String engineerName,
        String engineerEmail,
        String engineerPhone,
        String nextSteps
    ) {
        requireLogin();

        CaseApprovalDTO approval = new CaseApprovalDTO();
        approval.setCaseNumber(caseNumber);
        approval.setEngineerName(engineerName);
        approval.setEngineerEmail(engineerEmail);
        approval.setEngineerPhone(engineerPhone);
        approval.setNextSteps(nextSteps);

        CollaborationRequestStatusDTO body = caseResponsesApi.approveCollaborationRequest(caseToken, approval);
        if (body == null) {
            throw new IllegalStateException("Approve collaboration request returned empty response");
        }

        CollaborationRequestStatusDto approved = toCollaborationRequestDto(body);
        collaborationStorageService.storeFetched(List.of(approved));
        getResponses(caseToken);
        return approved;
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

    private CollaborationRequestStatusDto toCollaborationRequestDto(CollaborationRequestStatusDTO dto) {
        String status = dto.getStatus() != null ? dto.getStatus().getValue() : null;
        return new CollaborationRequestStatusDto(
            dto.getId(),
            status,
            dto.getSummary(),
            dto.getSubmitCompanyName(),
            dto.getSubmitCompanyId(),
            dto.getReceiveCompanyName(),
            dto.getReceiveCompanyId(),
            dto.getToken(),
            formatDateTime(dto.getCreatedAt()),
            formatDateTime(dto.getUpdatedAt())
        );
    }

    private static String formatDateTime(OffsetDateTime value) {
        return value != null ? value.toString() : null;
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
