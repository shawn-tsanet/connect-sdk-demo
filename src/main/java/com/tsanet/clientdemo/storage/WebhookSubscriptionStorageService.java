package com.tsanet.clientdemo.storage;

import com.tsanet.clientdemo.connectapi.dto.WebhookSubscriptionDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WebhookSubscriptionStorageService {
    private final WebhookSubscriptionRepository repository;

    public WebhookSubscriptionStorageService(WebhookSubscriptionRepository repository) {
        this.repository = repository;
    }

    public void storeFetched(List<WebhookSubscriptionDto> subscriptions) {
        repository.saveAll(subscriptions);
    }

    public List<WebhookSubscriptionDto> findAll() {
        return repository.findAll();
    }
}
