package com.tsanet.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tsanet")
public record TsaNetApplicationProperties(
    Api api,
    Auth auth,
    Storage storage
) {
    public record Api(String baseUrl) {
    }

    public record Auth(String username, String password) {
    }

    public record Storage(String sqlitePath) {
    }
}
