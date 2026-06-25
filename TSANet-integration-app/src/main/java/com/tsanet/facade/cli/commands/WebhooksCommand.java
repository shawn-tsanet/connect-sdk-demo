package com.tsanet.facade.cli.commands;

import com.tsanet.facade.cli.CliRunContext;
import com.tsanet.facade.cli.EntityPrinter;
import com.tsanet.api.TsaNetApiSession;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class WebhooksCommand implements Command {
    private final TsaNetApiSession session;
    private final CliRunContext cliRunContext;

    public WebhooksCommand(TsaNetApiSession session, CliRunContext cliRunContext) {
        this.session = session;
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
            EntityPrinter.printWebhooks(cliRunContext, "Webhook subscriptions", session.webhooks().listSubscriptions());
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
