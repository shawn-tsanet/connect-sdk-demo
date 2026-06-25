package com.tsanet.api.facade;

import com.tsanet.api.connectapi.dto.CollaborationRequestFormDto;
import com.tsanet.api.connectapi.dto.CollaborationRequestStatusDto;
import java.util.List;

public interface CollaborationRequestsFacade {
    List<CollaborationRequestStatusDto> listRequests();

    List<CollaborationRequestStatusDto> listStoredRequests();

    List<CollaborationRequestStatusDto> listStoredRequestsForCompany(long companyId);

    CollaborationRequestFormDto getCreateForm(long receiverCompanyId);

    List<CollaborationRequestFormDto> listStoredForms();

    List<CollaborationRequestFormDto> listStoredFormsForReceiver(long receiverCompanyId);

    CollaborationRequestStatusDto createRequest(
        long receiverCompanyId,
        String caseNumber,
        String summary,
        String description
    );

    void syncAllDetails();
}
