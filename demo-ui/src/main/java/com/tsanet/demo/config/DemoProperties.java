package com.tsanet.demo.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tsanet.demo")
public record DemoProperties(
    Map<String, EnvironmentDef> environments,
    String defaultEnvironment,
    String dataDir
) {

    public record EnvironmentDef(String label, String apiBaseUrl) {
    }

    public EnvironmentDef require(String key) {
        EnvironmentDef def = environments != null ? environments.get(key) : null;
        if (def == null) {
            throw new IllegalArgumentException("Unknown environment: " + key);
        }
        return def;
    }
}
