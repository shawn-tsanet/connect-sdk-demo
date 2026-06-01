package com.tsanet.clientdemo.connectapi.internal;

import com.tsanet.clientdemo.connectapi.dto.UserContextDto;
import com.tsanet.clientdemo.generated.api.IdentityApi;
import com.tsanet.clientdemo.generated.model.CompanyDTO;
import com.tsanet.clientdemo.generated.model.UserContextDTO;
import com.tsanet.clientdemo.generated.model.UserDTO;
import com.tsanet.clientdemo.storage.UserContextStorageService;
import org.springframework.stereotype.Component;

@Component
public class ConnectApiUserGateway {
    private final IdentityApi identityApi;
    private final ConnectApiSessionStore sessionStore;
    private final UserContextStorageService storageService;

    public ConnectApiUserGateway(
        IdentityApi identityApi,
        ConnectApiSessionStore sessionStore,
        UserContextStorageService storageService
    ) {
        this.identityApi = identityApi;
        this.sessionStore = sessionStore;
        this.storageService = storageService;
    }

    public UserContextDto getCurrentUser() {
        requireLogin();

        UserContextDTO body = identityApi.getCurrentUser();
        if (body == null) {
            throw new IllegalStateException("Current user response is empty");
        }
        UserContextDto userContext = toDto(body);
        storageService.storeFetched(userContext);
        return userContext;
    }

    private UserContextDto toDto(UserContextDTO dto) {
        CompanyDTO company = dto.getCompany();
        UserDTO user = dto.getUser();
        return new UserContextDto(
            company != null ? company.getId() : null,
            company != null ? company.getName() : null,
            user != null ? user.getId() : null,
            user != null ? user.getUsername() : null,
            user != null ? user.getEmail() : null,
            user != null ? user.getFirstName() : null,
            user != null ? user.getLastName() : null
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
