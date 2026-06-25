package com.tsanet.api.facade;

import com.tsanet.api.connectapi.dto.WebhookSubscriptionDto;
import com.tsanet.api.connectapi.dto.WebhookSubscriptionResponseDto;
import java.util.List;

public interface WebhooksFacade {
    List<WebhookSubscriptionDto> listSubscriptions();

    List<WebhookSubscriptionDto> listStoredSubscriptions();

    WebhookSubscriptionResponseDto createSubscription(String callbackUrl, List<String> eventTypes);

    void deleteSubscription(Long id);
}
