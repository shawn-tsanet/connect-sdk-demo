package com.tsanet.facade.config;

import com.tsanet.api.TsaNetApi;
import com.tsanet.api.TsaNetApiConnectionSettings;
import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.TsaNetApiSessionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ConnectFacadeProperties.class, CliProperties.class, WebhookProperties.class})
public class ConnectFacadeConfiguration {
    @Bean
    TsaNetApiSessionFactory tsaNetApiSessionFactory(ConnectFacadeProperties properties) {
        return TsaNetApi.sessionFactory(TsaNetApiConnectionSettings.of(
            properties.api().baseUrl(),
            properties.storage().sqlitePath()
        ));
    }

    @Bean
    TsaNetApiSession tsaNetApiSession(
        TsaNetApiSessionFactory sessionFactory,
        ConnectFacadeProperties properties
    ) {
        return sessionFactory.openSession(
            "default",
            properties.auth().username(),
            properties.auth().password()
        );
    }
}
