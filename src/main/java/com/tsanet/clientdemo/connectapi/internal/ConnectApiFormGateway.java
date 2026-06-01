package com.tsanet.clientdemo.connectapi.internal;

import com.tsanet.clientdemo.generated.api.FormRequestApi;
import com.tsanet.clientdemo.generated.model.CollaborationRequestDTO;
import org.springframework.stereotype.Component;

@Component
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
