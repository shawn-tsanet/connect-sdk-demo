package com.tsanet.clientdemo.storage;

import com.tsanet.clientdemo.connectapi.dto.CaseResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CaseResponseStorageService {
    private final CaseResponseRepository repository;

    public CaseResponseStorageService(CaseResponseRepository repository) {
        this.repository = repository;
    }

    public void storeFetched(List<CaseResponseDto> responses) {
        repository.saveAll(responses);
    }

    public List<CaseResponseDto> findAll() {
        return repository.findAll();
    }

    public List<CaseResponseDto> findByCaseToken(String caseToken) {
        return repository.findByCaseToken(caseToken);
    }
}
