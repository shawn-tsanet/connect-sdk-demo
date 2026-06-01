package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class WebhooksCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public WebhooksCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "webhooks";
    }

    @Override
    public String description() {
        return "Fetch webhook subscriptions from Connect API";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            EntityPrinter.printWebhooks(cliRunContext, "Webhook subscriptions", connectApiClient.getWebhookSubscriptions());
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
