package com.tsanet.demo.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Properties;
import org.springframework.stereotype.Component;

/**
 * Holds BETA member credentials entered via the settings UI. Stored in a
 * plain properties file outside the repo (never logged, never returned by
 * the settings API) so the demo can run without env vars or a redeploy.
 */
@Component
public class CredentialsStore {

    private final Path path;

    public CredentialsStore(DemoProperties properties) {
        this.path = Path.of(properties.credentialsPath());
    }

    public synchronized Optional<Credentials> load() {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (var in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String username = props.getProperty("username");
        String password = props.getProperty("password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new Credentials(username, password));
    }

    public synchronized void save(String username, String password) {
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);
        try {
            Files.createDirectories(path.getParent());
            try (var out = Files.newOutputStream(path)) {
                props.store(out, "TSANet demo BETA credentials - do not commit");
            }
            if (Files.getFileStore(path).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void clear() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record Credentials(String username, String password) {
    }
}
