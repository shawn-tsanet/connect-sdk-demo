package com.tsanet.clientdemo.connectapi;

import com.tsanet.clientdemo.connectapi.dto.CaseNoteDto;
import com.tsanet.clientdemo.connectapi.dto.CaseResponseDto;
import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestFormDto;
import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestStatusDto;
import com.tsanet.clientdemo.connectapi.dto.PartnerSelectionDto;
import com.tsanet.clientdemo.connectapi.dto.UserContextDto;
import com.tsanet.clientdemo.connectapi.dto.WebhookSubscriptionDto;
import java.util.List;
import java.util.Optional;

public interface ConnectApiClient {
    String login(String username, String password);

    boolean isAuthorized();

    Optional<String> currentUsername();

    Optional<String> currentBearerToken();

    void logout();

    List<CollaborationRequestStatusDto> getCollaborationRequests();

    List<CollaborationRequestStatusDto> getStoredCollaborationRequests();

    List<CollaborationRequestStatusDto> getStoredCollaborationRequests(long companyId);

    List<CaseNoteDto> getNotes(String caseToken);

    List<CaseNoteDto> getNotesForAllRequests();

    List<CaseNoteDto> getStoredNotes();

    List<CaseNoteDto> getStoredNotes(String caseToken);

    List<CaseResponseDto> getResponses(String caseToken);

    List<CaseResponseDto> getResponsesForAllRequests();

    List<CaseResponseDto> getStoredResponses();

    List<CaseResponseDto> getStoredResponses(String caseToken);

    UserContextDto getCurrentUser();

    List<UserContextDto> getStoredCurrentUser();

    List<WebhookSubscriptionDto> getWebhookSubscriptions();

    List<WebhookSubscriptionDto> getStoredWebhookSubscriptions();

    List<PartnerSelectionDto> searchPartners(String searchTerm);

    List<PartnerSelectionDto> getStoredPartners();

    List<PartnerSelectionDto> getStoredPartners(String searchTerm);

    CollaborationRequestFormDto getCollaborationRequestForm(long receiverCompanyId);

    CollaborationRequestStatusDto createCollaborationRequest(
        long receiverCompanyId,
        String caseNumber,
        String summary,
        String description
    );

    void syncAllRequestDetails();
}
