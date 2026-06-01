package com.tsanet.clientdemo.connectapi.internal;

import static com.tsanet.clientdemo.connectapi.internal.OpenApiMapping.dateTime;
import static com.tsanet.clientdemo.connectapi.internal.OpenApiMapping.joinEnumList;

import com.tsanet.clientdemo.connectapi.dto.WebhookSubscriptionDto;
import com.tsanet.clientdemo.generated.api.WebhooksApi;
import com.tsanet.clientdemo.generated.model.WebhookSubscriptionDTO;
import com.tsanet.clientdemo.storage.WebhookSubscriptionStorageService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConnectApiWebhooksGateway {
    private final WebhooksApi webhooksApi;
    private final ConnectApiSessionStore sessionStore;
    private final WebhookSubscriptionStorageService storageService;

    public ConnectApiWebhooksGateway(
        WebhooksApi webhooksApi,
        ConnectApiSessionStore sessionStore,
        WebhookSubscriptionStorageService storageService
    ) {
        this.webhooksApi = webhooksApi;
        this.sessionStore = sessionStore;
        this.storageService = storageService;
    }

    public List<WebhookSubscriptionDto> listWebhookSubscriptions() {
        requireLogin();

        List<WebhookSubscriptionDTO> body = webhooksApi.listWebhookSubscriptions();
        if (body == null) {
            return Collections.emptyList();
        }
        List<WebhookSubscriptionDto> subscriptions = body.stream().map(this::toDto).toList();
        storageService.storeFetched(subscriptions);
        return subscriptions;
    }

    private WebhookSubscriptionDto toDto(WebhookSubscriptionDTO dto) {
        return new WebhookSubscriptionDto(
            dto.getId(),
            dto.getCallbackUrl(),
            joinEnumList(dto.getEventTypes()),
            dto.getActive(),
            dateTime(dto.getCreatedAt()),
            dateTime(dto.getUpdatedAt())
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
