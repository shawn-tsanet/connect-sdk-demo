package com.tsanet.clientdemo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ApiProperties.class, AuthProperties.class, CrashProperties.class, StorageProperties.class})
public class AppConfig {
}
