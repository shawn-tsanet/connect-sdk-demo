package com.tsanet.facade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tsanet")
public record ConnectFacadeProperties(
    Api api,
    Auth auth,
    Storage storage
) {
    public record Api(String baseUrl) {
    }

    public record Auth(String username, String password) {
        public boolean isConfigured() {
            return username != null && !username.isBlank() && password != null && !password.isBlank();
        }
    }

    public record Storage(String sqlitePath) {
    }
}
