package com.tsanet.demo.config;

import com.tsanet.api.TsaNetApi;
import com.tsanet.api.TsaNetApiConfiguration;
import com.tsanet.api.TsaNetApiSession;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Owns the active-environment state and one lazily-created SDK session per
 * environment. Each environment gets an isolated SQLite cache and credentials
 * file under the data dir, so BETA and DEV data never mix. The active
 * environment persists across restarts via a marker file.
 */
@Component
public class EnvironmentService {

    private final DemoProperties properties;
    private final Map<String, TsaNetApiSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, CredentialsStore> credentialStores = new ConcurrentHashMap<>();
    private final Path activeEnvFile;
    private volatile String activeEnvironment;

    public EnvironmentService(DemoProperties properties) {
        this.properties = properties;
        this.activeEnvFile = dataDir().resolve("active-environment");
        this.activeEnvironment = loadPersistedEnvironment();
    }

    private Path dataDir() {
        return Path.of(properties.dataDir());
    }

    private String loadPersistedEnvironment() {
        try {
            if (Files.exists(activeEnvFile)) {
                String persisted = Files.readString(activeEnvFile).trim();
                if (properties.environments().containsKey(persisted)) {
                    return persisted;
                }
            }
        } catch (IOException ignored) {
            // fall through to default
        }
        return properties.defaultEnvironment();
    }

    public String activeEnvironment() {
        return activeEnvironment;
    }

    public DemoProperties.EnvironmentDef activeDefinition() {
        return properties.require(activeEnvironment);
    }

    public Map<String, DemoProperties.EnvironmentDef> environments() {
        return properties.environments();
    }

    public synchronized void switchTo(String key) {
        properties.require(key);
        this.activeEnvironment = key;
        try {
            Files.createDirectories(activeEnvFile.getParent());
            Files.writeString(activeEnvFile, key);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Session for the active environment (created on first use). */
    public TsaNetApiSession currentSession() {
        return sessionFor(activeEnvironment);
    }

    public TsaNetApiSession sessionFor(String key) {
        DemoProperties.EnvironmentDef def = properties.require(key);
        return sessions.computeIfAbsent(key, k -> TsaNetApi.initialize(TsaNetApiConfiguration.of(
            def.apiBaseUrl(),
            dataDir().resolve("data-" + k + ".db").toString(),
            null,
            null
        )));
    }

    /** Credentials store for the active environment. */
    public CredentialsStore currentCredentials() {
        return credentialsFor(activeEnvironment);
    }

    public CredentialsStore credentialsFor(String key) {
        properties.require(key);
        return credentialStores.computeIfAbsent(key,
            k -> new CredentialsStore(dataDir().resolve("credentials-" + k + ".properties")));
    }
}
