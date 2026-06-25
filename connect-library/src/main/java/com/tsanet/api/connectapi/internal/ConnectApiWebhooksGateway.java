package com.tsanet.api.connectapi.internal;

import static com.tsanet.api.connectapi.internal.OpenApiMapping.dateTime;
import static com.tsanet.api.connectapi.internal.OpenApiMapping.joinEnumList;

import com.tsanet.api.connectapi.dto.WebhookSubscriptionDto;
import com.tsanet.api.connectapi.dto.WebhookSubscriptionResponseDto;
import com.tsanet.api.generated.api.WebhooksApi;
import com.tsanet.api.generated.model.CreateWebhookSubscriptionRequestDTO;
import com.tsanet.api.generated.model.WebhookSubscriptionDTO;
import com.tsanet.api.generated.model.WebhookSubscriptionResponseDTO;
import com.tsanet.api.generated.model.WebhookEventType;
import com.tsanet.api.storage.WebhookSubscriptionStorageService;
import java.net.URI;
import java.util.Collections;
import java.util.List;

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

    public WebhookSubscriptionResponseDto createWebhookSubscription(String callbackUrl, List<String> eventTypes) {
        requireLogin();

        CreateWebhookSubscriptionRequestDTO request = new CreateWebhookSubscriptionRequestDTO();
        request.setCallbackUrl(URI.create(callbackUrl));
        if (eventTypes != null && !eventTypes.isEmpty()) {
            request.setEventTypes(eventTypes.stream().map(WebhookEventType::fromValue).toList());
        }

        WebhookSubscriptionResponseDTO response = webhooksApi.createWebhookSubscription(request);
        if (response == null) {
            throw new IllegalStateException("Create webhook subscription returned empty response");
        }

        // Refresh stored list after mutation.
        listWebhookSubscriptions();
        return toResponseDto(response);
    }

    public void deleteWebhookSubscription(Long id) {
        requireLogin();
        webhooksApi.deleteWebhookSubscription(id);
        // Refresh stored list after mutation.
        listWebhookSubscriptions();
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

    private WebhookSubscriptionResponseDto toResponseDto(WebhookSubscriptionResponseDTO dto) {
        return new WebhookSubscriptionResponseDto(
            dto.getId(),
            dto.getCallbackUrl(),
            joinEnumList(dto.getEventTypes()),
            dto.getActive(),
            dto.getSecret(),
            dateTime(dto.getCreatedAt())
        );
    }

    private void requireLogin() {
        sessionStore.getBearerToken().orElseThrow(() -> new IllegalStateException("Not logged in"));
    }
}
