package com.tsanet.clientdemo.storage;

import com.tsanet.clientdemo.connectapi.dto.UserContextDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserContextStorageService {
    private final UserContextRepository repository;

    public UserContextStorageService(UserContextRepository repository) {
        this.repository = repository;
    }

    public void storeFetched(UserContextDto userContext) {
        repository.save(userContext);
    }

    public List<UserContextDto> findAll() {
        return repository.findAll();
    }
}
