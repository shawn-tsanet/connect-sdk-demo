package com.tsanet.clientdemo.storage;

import com.tsanet.clientdemo.connectapi.dto.PartnerSelectionDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PartnerSelectionStorageService {
    private final PartnerSelectionRepository repository;

    public PartnerSelectionStorageService(PartnerSelectionRepository repository) {
        this.repository = repository;
    }

    public void storeFetched(String searchTerm, List<PartnerSelectionDto> partners) {
        repository.replaceForSearchTerm(searchTerm, partners);
    }

    public List<PartnerSelectionDto> findAll() {
        return repository.findAll();
    }

    public List<PartnerSelectionDto> findBySearchTerm(String searchTerm) {
        return repository.findBySearchTerm(searchTerm);
    }
}
