package com.tsanet.clientdemo.config;

import com.tsanet.clientdemo.connectapi.internal.ConnectApiSessionStore;
import com.tsanet.clientdemo.generated.api.CaseNotesApi;
import com.tsanet.clientdemo.generated.api.CollaborationRequestsApi;
import com.tsanet.clientdemo.generated.api.EntitySearchApi;
import com.tsanet.clientdemo.generated.api.FormRequestApi;
import com.tsanet.clientdemo.generated.api.IdentityApi;
import com.tsanet.clientdemo.generated.api.WebhooksApi;
import com.tsanet.clientdemo.generated.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectOpenApiConfiguration {

    @Bean
    public ApiClient connectApiClient(ApiProperties apiProperties, ConnectApiSessionStore sessionStore) {
        ApiClient client = new ApiClient();
        client.setBasePath(apiProperties.baseUrl());
        client.setBearerToken(() -> sessionStore.getBearerToken().orElse(null));
        return client;
    }

    @Bean
    public IdentityApi identityApi(ApiClient connectApiClient) {
        return new IdentityApi(connectApiClient);
    }

    @Bean
    public CollaborationRequestsApi collaborationRequestsApi(ApiClient connectApiClient) {
        return new CollaborationRequestsApi(connectApiClient);
    }

    @Bean
    public CaseNotesApi caseNotesApi(ApiClient connectApiClient) {
        return new CaseNotesApi(connectApiClient);
    }

    @Bean
    public WebhooksApi webhooksApi(ApiClient connectApiClient) {
        return new WebhooksApi(connectApiClient);
    }

    @Bean
    public EntitySearchApi entitySearchApi(ApiClient connectApiClient) {
        return new EntitySearchApi(connectApiClient);
    }

    @Bean
    public FormRequestApi formRequestApi(ApiClient connectApiClient) {
        return new FormRequestApi(connectApiClient);
    }
}
