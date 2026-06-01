package com.tsanet.clientdemo.connectapi.internal;

import com.tsanet.clientdemo.generated.api.IdentityApi;
import com.tsanet.clientdemo.generated.model.LoginRequestDTO;
import com.tsanet.clientdemo.generated.model.TokenDTO;
import org.springframework.stereotype.Component;

@Component
public class ConnectApiAuthGateway {
    private final IdentityApi identityApi;

    public ConnectApiAuthGateway(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    public String login(String username, String password) {
        LoginRequestDTO request = new LoginRequestDTO().username(username).password(password);
        TokenDTO response = identityApi.login(request);
        if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
            throw new IllegalStateException("Login succeeded but accessToken is missing");
        }
        return response.getAccessToken();
    }
}
