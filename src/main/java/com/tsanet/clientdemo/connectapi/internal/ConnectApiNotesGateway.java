package com.tsanet.clientdemo.connectapi.internal;

import static com.tsanet.clientdemo.connectapi.internal.OpenApiMapping.dateTime;
import static com.tsanet.clientdemo.connectapi.internal.OpenApiMapping.enumValue;

import com.tsanet.clientdemo.connectapi.dto.CaseNoteDto;
import com.tsanet.clientdemo.generated.api.CaseNotesApi;
import com.tsanet.clientdemo.generated.model.CaseNoteDTO;
import com.tsanet.clientdemo.storage.CaseNoteStorageService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConnectApiNotesGateway {
    private final CaseNotesApi caseNotesApi;
    private final ConnectApiSessionStore sessionStore;
    private final CaseNoteStorageService storageService;

    public ConnectApiNotesGateway(
        CaseNotesApi caseNotesApi,
        ConnectApiSessionStore sessionStore,
        CaseNoteStorageService storageService
    ) {
        this.caseNotesApi = caseNotesApi;
        this.sessionStore = sessionStore;
        this.storageService = storageService;
    }

    public List<CaseNoteDto> getNotes(String caseToken) {
        requireLogin();

        List<CaseNoteDTO> body = caseNotesApi.getNotes(caseToken, null, null, false);
        if (body == null) {
            return Collections.emptyList();
        }
        List<CaseNoteDto> notes = body.stream().map(note -> toDto(note, caseToken)).toList();
        storageService.storeFetched(notes);
        return notes;
    }

    private CaseNoteDto toDto(CaseNoteDTO dto, String caseToken) {
        return new CaseNoteDto(
            dto.getId(),
            dto.getCaseId(),
            caseToken,
            dto.getCompanyName(),
            dto.getCreatorUsername(),
            dto.getCreatorEmail(),
            dto.getCreatorName(),
            dto.getSummary(),
            dto.getDescription(),
            enumValue(dto.getPriority()),
            enumValue(dto.getStatus()),
            dto.getToken(),
            dateTime(dto.getCreatedAt()),
            dateTime(dto.getUpdatedAt())
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
