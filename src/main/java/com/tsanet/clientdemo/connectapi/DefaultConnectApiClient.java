package com.tsanet.clientdemo.connectapi;

import com.tsanet.clientdemo.connectapi.dto.CaseNoteDto;
import com.tsanet.clientdemo.connectapi.dto.CaseResponseDto;
import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestFormDto;
import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestStatusDto;
import com.tsanet.clientdemo.connectapi.dto.PartnerSelectionDto;
import com.tsanet.clientdemo.connectapi.dto.UserContextDto;
import com.tsanet.clientdemo.connectapi.dto.WebhookSubscriptionDto;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiAuthGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiCollaborationGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiFormGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiNotesGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiPartnersGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiResponsesGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiSessionStore;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiUserGateway;
import com.tsanet.clientdemo.connectapi.internal.ConnectApiWebhooksGateway;
import com.tsanet.clientdemo.generated.model.CasePriority;
import com.tsanet.clientdemo.generated.model.CollaborationRequestDTO;
import com.tsanet.clientdemo.storage.CaseNoteStorageService;
import com.tsanet.clientdemo.storage.CaseResponseStorageService;
import com.tsanet.clientdemo.storage.CollaborationRequestStorageService;
import com.tsanet.clientdemo.storage.PartnerSelectionStorageService;
import com.tsanet.clientdemo.storage.UserContextStorageService;
import com.tsanet.clientdemo.storage.WebhookSubscriptionStorageService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultConnectApiClient implements ConnectApiClient {
    @Autowired
    private ConnectApiAuthGateway authGateway;
    @Autowired
    private ConnectApiCollaborationGateway collaborationGateway;
    @Autowired
    private ConnectApiFormGateway formGateway;
    @Autowired
    private ConnectApiNotesGateway notesGateway;
    @Autowired
    private ConnectApiResponsesGateway responsesGateway;
    @Autowired
    private ConnectApiUserGateway userGateway;
    @Autowired
    private ConnectApiWebhooksGateway webhooksGateway;
    @Autowired
    private ConnectApiPartnersGateway partnersGateway;
    @Autowired
    private ConnectApiSessionStore sessionStore;
    @Autowired
    private CollaborationRequestStorageService collaborationRequestStorageService;
    @Autowired
    private CaseNoteStorageService caseNoteStorageService;
    @Autowired
    private CaseResponseStorageService caseResponseStorageService;
    @Autowired
    private UserContextStorageService userContextStorageService;
    @Autowired
    private WebhookSubscriptionStorageService webhookSubscriptionStorageService;
    @Autowired
    private PartnerSelectionStorageService partnerSelectionStorageService;

    @Override
    public String login(String username, String password) {
        String token = authGateway.login(username, password);
        sessionStore.save(username, token);
        return token;
    }

    @Override
    public boolean isAuthorized() {
        return sessionStore.isAuthorized();
    }

    @Override
    public Optional<String> currentUsername() {
        return sessionStore.getUsername();
    }

    @Override
    public Optional<String> currentBearerToken() {
        return sessionStore.getBearerToken();
    }

    @Override
    public void logout() {
        sessionStore.clear();
    }

    @Override
    public List<CollaborationRequestStatusDto> getCollaborationRequests() {
        return collaborationGateway.getCollaborationRequests();
    }

    @Override
    public List<CollaborationRequestStatusDto> getStoredCollaborationRequests() {
        return collaborationRequestStorageService.findAll();
    }

    @Override
    public List<CollaborationRequestStatusDto> getStoredCollaborationRequests(long companyId) {
        return collaborationRequestStorageService.findByCompanyId(companyId);
    }

    @Override
    public List<CaseNoteDto> getNotes(String caseToken) {
        return notesGateway.getNotes(caseToken);
    }

    @Override
    public List<CaseNoteDto> getNotesForAllRequests() {
        return fetchNotesForRequests(getCollaborationRequests());
    }

    @Override
    public List<CaseNoteDto> getStoredNotes() {
        return caseNoteStorageService.findAll();
    }

    @Override
    public List<CaseNoteDto> getStoredNotes(String caseToken) {
        return caseNoteStorageService.findByCaseToken(caseToken);
    }

    @Override
    public List<CaseResponseDto> getResponses(String caseToken) {
        return responsesGateway.getResponses(caseToken);
    }

    @Override
    public List<CaseResponseDto> getResponsesForAllRequests() {
        return fetchResponsesForRequests(getCollaborationRequests());
    }

    @Override
    public List<CaseResponseDto> getStoredResponses() {
        return caseResponseStorageService.findAll();
    }

    @Override
    public List<CaseResponseDto> getStoredResponses(String caseToken) {
        return caseResponseStorageService.findByCaseToken(caseToken);
    }

    @Override
    public UserContextDto getCurrentUser() {
        return userGateway.getCurrentUser();
    }

    @Override
    public List<UserContextDto> getStoredCurrentUser() {
        return userContextStorageService.findAll();
    }

    @Override
    public List<WebhookSubscriptionDto> getWebhookSubscriptions() {
        return webhooksGateway.listWebhookSubscriptions();
    }

    @Override
    public List<WebhookSubscriptionDto> getStoredWebhookSubscriptions() {
        return webhookSubscriptionStorageService.findAll();
    }

    @Override
    public List<PartnerSelectionDto> searchPartners(String searchTerm) {
        return partnersGateway.searchPartners(searchTerm);
    }

    @Override
    public List<PartnerSelectionDto> getStoredPartners() {
        return partnerSelectionStorageService.findAll();
    }

    @Override
    public List<PartnerSelectionDto> getStoredPartners(String searchTerm) {
        return partnerSelectionStorageService.findBySearchTerm(searchTerm);
    }

    @Override
    public CollaborationRequestFormDto getCollaborationRequestForm(long receiverCompanyId) {
        CollaborationRequestDTO form = formGateway.getFormByCompanyId(receiverCompanyId);
        if (form.getDocumentId() == null) {
            throw new IllegalStateException("Form for company " + receiverCompanyId + " has no documentId");
        }
        int customFieldCount = form.getCustomFields() != null ? form.getCustomFields().size() : 0;
        return new CollaborationRequestFormDto(receiverCompanyId, form.getDocumentId(), customFieldCount);
    }

    @Override
    public CollaborationRequestStatusDto createCollaborationRequest(
        long receiverCompanyId,
        String caseNumber,
        String summary,
        String description
    ) {
        CollaborationRequestDTO form = formGateway.getFormByCompanyId(receiverCompanyId);
        form.setInternalCaseNumber(caseNumber);
        form.setProblemSummary(summary);
        form.setProblemDescription(description);
        form.setTestSubmission(true);
        if (form.getPriority() == null) {
            form.setPriority(CasePriority.MEDIUM);
        }
        if (form.getCustomFields() == null) {
            form.setCustomFields(Collections.emptyList());
        }
        if (form.getInternalNotes() == null) {
            form.setInternalNotes(Collections.emptyList());
        }
        return collaborationGateway.createCollaborationRequest(form);
    }

    @Override
    public void syncAllRequestDetails() {
        List<CollaborationRequestStatusDto> requests = getCollaborationRequests();
        fetchNotesForRequests(requests);
        fetchResponsesForRequests(requests);
    }

    private List<CaseNoteDto> fetchNotesForRequests(List<CollaborationRequestStatusDto> requests) {
        List<CaseNoteDto> allNotes = new ArrayList<>();
        for (CollaborationRequestStatusDto request : requests) {
            if (request.token() == null || request.token().isBlank()) {
                continue;
            }
            allNotes.addAll(notesGateway.getNotes(request.token()));
        }
        return allNotes;
    }

    private List<CaseResponseDto> fetchResponsesForRequests(List<CollaborationRequestStatusDto> requests) {
        List<CaseResponseDto> allResponses = new ArrayList<>();
        for (CollaborationRequestStatusDto request : requests) {
            if (request.token() == null || request.token().isBlank()) {
                continue;
            }
            allResponses.addAll(responsesGateway.getResponses(request.token()));
        }
        return allResponses;
    }
}
