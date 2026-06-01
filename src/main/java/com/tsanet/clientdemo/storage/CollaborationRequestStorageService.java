package com.tsanet.clientdemo.storage;

import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestStatusDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CollaborationRequestStorageService {
    private final CollaborationRequestRepository repository;

    public CollaborationRequestStorageService(CollaborationRequestRepository repository) {
        this.repository = repository;
    }

    public void storeFetched(List<CollaborationRequestStatusDto> requests) {
        repository.saveAll(requests);
    }

    public List<CollaborationRequestStatusDto> findAll() {
        return repository.findAll();
    }

    public List<CollaborationRequestStatusDto> findByCompanyId(long companyId) {
        return repository.findByCompanyId(companyId);
    }
}
