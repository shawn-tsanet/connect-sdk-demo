package com.tsanet.clientdemo.config;

import org.crsh.spring.SpringBootstrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "crash.enabled", havingValue = "true")
public class CrashConfiguration {

    @Bean(destroyMethod = "destroy")
    public SpringBootstrap crashSpringBootstrap(CrashProperties crashProperties) {
        System.setProperty("crash.ssh.port", String.valueOf(crashProperties.sshPort()));
        System.setProperty("crash.auth", "simple");
        System.setProperty("crash.auth.simple.username", crashProperties.authUsername());
        System.setProperty("crash.auth.simple.password", crashProperties.authPassword());
        return new SpringBootstrap();
    }
}
