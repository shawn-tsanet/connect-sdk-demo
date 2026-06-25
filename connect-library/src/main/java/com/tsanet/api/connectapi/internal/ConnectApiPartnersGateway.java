package com.tsanet.api.connectapi.internal;

import com.tsanet.api.connectapi.dto.PartnerSelectionDto;
import com.tsanet.api.generated.api.EntitySearchApi;
import com.tsanet.api.generated.model.PartnerSelectionDTO;
import com.tsanet.api.storage.PartnerSelectionStorageService;
import java.util.Collections;
import java.util.List;

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
