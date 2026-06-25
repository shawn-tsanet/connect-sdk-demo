package com.tsanet.facade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ConnectFacadeApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ConnectFacadeApplication.class)
            .web(org.springframework.boot.WebApplicationType.NONE)
            .run(args);

        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }
}
