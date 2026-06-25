package com.tsanet.api.connectapi.internal;

import com.tsanet.api.generated.api.FormRequestApi;
import com.tsanet.api.generated.model.CollaborationRequestDTO;

public class ConnectApiFormGateway {
    private final FormRequestApi formRequestApi;
    private final ConnectApiSessionStore sessionStore;

    public ConnectApiFormGateway(FormRequestApi formRequestApi, ConnectApiSessionStore sessionStore) {
        this.formRequestApi = formRequestApi;
        this.sessionStore = sessionStore;
    }

    public CollaborationRequestDTO getFormByCompanyId(long companyId) {
        requireLogin();
        return formRequestApi.getFormByCompanyId(companyId);
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
