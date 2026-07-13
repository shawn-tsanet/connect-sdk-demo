package com.tsanet.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tsanet.demo")
public record DemoProperties(
    String apiBaseUrl,
    String sqlitePath,
    String credentialsPath
) {
}
