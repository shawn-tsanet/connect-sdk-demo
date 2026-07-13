package com.tsanet.demo.config;

import com.tsanet.api.TsaNetApi;
import com.tsanet.api.TsaNetApiConfiguration;
import com.tsanet.api.TsaNetApiSession;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoProperties.class)
public class DemoSessionConfiguration {

    @Bean
    public TsaNetApiSession tsaNetApiSession(DemoProperties properties) {
        return TsaNetApi.initialize(TsaNetApiConfiguration.of(
            properties.apiBaseUrl(),
            properties.sqlitePath(),
            null,
            null
        ));
    }
}
