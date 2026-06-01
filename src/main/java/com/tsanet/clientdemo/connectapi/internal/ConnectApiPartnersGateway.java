package com.tsanet.clientdemo.connectapi.internal;

import com.tsanet.clientdemo.connectapi.dto.PartnerSelectionDto;
import com.tsanet.clientdemo.generated.api.EntitySearchApi;
import com.tsanet.clientdemo.generated.model.PartnerSelectionDTO;
import com.tsanet.clientdemo.storage.PartnerSelectionStorageService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConnectApiPartnersGateway {
    private final EntitySearchApi entitySearchApi;
    private final ConnectApiSessionStore sessionStore;
    private final PartnerSelectionStorageService storageService;

    public ConnectApiPartnersGateway(
        EntitySearchApi entitySearchApi,
        ConnectApiSessionStore sessionStore,
        PartnerSelectionStorageService storageService
    ) {
        this.entitySearchApi = entitySearchApi;
        this.sessionStore = sessionStore;
        this.storageService = storageService;
    }

    public List<PartnerSelectionDto> searchPartners(String searchTerm) {
        requireLogin();

        List<PartnerSelectionDTO> body = entitySearchApi.searchPartners(searchTerm);
        if (body == null) {
            return Collections.emptyList();
        }
        List<PartnerSelectionDto> partners = body.stream().map(partner -> toDto(partner, searchTerm)).toList();
        storageService.storeFetched(searchTerm, partners);
        return partners;
    }

    private PartnerSelectionDto toDto(PartnerSelectionDTO dto, String searchTerm) {
        return new PartnerSelectionDto(
            searchTerm,
            dto.getLabel(),
            dto.getCompanyName(),
            dto.getDepartmentName(),
            dto.getCompanyId(),
            dto.getDepartmentId(),
            dto.getDocumentId()
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
