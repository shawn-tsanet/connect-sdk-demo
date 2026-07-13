package com.tsanet.demo.web;

import com.tsanet.demo.config.CredentialsStore;
import com.tsanet.demo.config.EnvironmentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SettingsController {

    private final EnvironmentService environments;

    public SettingsController(EnvironmentService environments) {
        this.environments = environments;
    }

    @GetMapping("/api/settings")
    public SettingsStatus getSettings() {
        List<EnvironmentStatus> envs = environments.environments().entrySet().stream()
            .map(e -> {
                var creds = environments.credentialsFor(e.getKey()).load();
                return new EnvironmentStatus(
                    e.getKey(),
                    e.getValue().label(),
                    e.getValue().apiBaseUrl(),
                    creds.isPresent(),
                    creds.map(CredentialsStore.Credentials::username).orElse(null)
                );
            })
            .toList();
        return new SettingsStatus(environments.activeEnvironment(), envs);
    }

    @PostMapping("/api/settings/environment")
    public SettingsStatus switchEnvironment(@RequestBody SwitchBody body) {
        try {
            environments.switchTo(body.environment());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return getSettings();
    }

    @PostMapping("/api/settings/{env}/credentials")
    public SettingsStatus saveCredentials(@PathVariable String env, @RequestBody SaveCredentialsBody body) {
        if (body.username() == null || body.username().isBlank()
            || body.password() == null || body.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password are required");
        }
        try {
            environments.credentialsFor(env).save(body.username(), body.password());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        environments.sessionFor(env).auth().logout();
        return getSettings();
    }

    @DeleteMapping("/api/settings/{env}/credentials")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCredentials(@PathVariable String env) {
        try {
            environments.credentialsFor(env).clear();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        environments.sessionFor(env).auth().logout();
    }

    public record SwitchBody(String environment) {
    }

    public record SaveCredentialsBody(String username, String password) {
    }

    public record EnvironmentStatus(
        String key,
        String label,
        String apiBaseUrl,
        boolean configured,
        String username
    ) {
    }

    public record SettingsStatus(String activeEnvironment, List<EnvironmentStatus> environments) {
    }
}
