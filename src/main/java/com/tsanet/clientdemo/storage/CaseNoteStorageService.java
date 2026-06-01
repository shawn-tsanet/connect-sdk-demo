package com.tsanet.clientdemo.storage;

import com.tsanet.clientdemo.connectapi.dto.CaseNoteDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CaseNoteStorageService {
    private final CaseNoteRepository repository;

    public CaseNoteStorageService(CaseNoteRepository repository) {
        this.repository = repository;
    }

    public void storeFetched(List<CaseNoteDto> notes) {
        repository.saveAll(notes);
    }

    public List<CaseNoteDto> findAll() {
        return repository.findAll();
    }

    public List<CaseNoteDto> findByCaseToken(String caseToken) {
        return repository.findByCaseToken(caseToken);
    }
}
