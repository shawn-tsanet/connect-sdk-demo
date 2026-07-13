package com.tsanet.demo.web;

import com.tsanet.api.TsaNetApiSession;
import com.tsanet.demo.config.CredentialsStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SettingsController {

    private final CredentialsStore credentialsStore;
    private final TsaNetApiSession session;

    public SettingsController(CredentialsStore credentialsStore, TsaNetApiSession session) {
        this.credentialsStore = credentialsStore;
        this.session = session;
    }

    @GetMapping("/api/settings")
    public SettingsStatus getSettings() {
        return credentialsStore.load()
            .map(c -> new SettingsStatus(true, c.username()))
            .orElseGet(() -> new SettingsStatus(false, null));
    }

    @PostMapping("/api/settings")
    public SettingsStatus saveSettings(@RequestBody SaveSettingsBody body) {
        if (body.username() == null || body.username().isBlank()
            || body.password() == null || body.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password are required");
        }
        credentialsStore.save(body.username(), body.password());
        session.auth().logout();
        return getSettings();
    }

    @DeleteMapping("/api/settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearSettings() {
        credentialsStore.clear();
        session.auth().logout();
    }

    public record SaveSettingsBody(String username, String password) {
    }

    public record SettingsStatus(boolean configured, String username) {
    }
}
