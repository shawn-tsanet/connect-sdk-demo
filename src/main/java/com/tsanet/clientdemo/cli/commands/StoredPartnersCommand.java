package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliArgs;
import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class StoredPartnersCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public StoredPartnersCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "stored-partners";
    }

    @Override
    public String description() {
        return "List partner search results stored in SQLite (--search TERM optional)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            var partners = CliArgs.search(args)
                .map(connectApiClient::getStoredPartners)
                .orElseGet(connectApiClient::getStoredPartners);
            EntityPrinter.printPartners(cliRunContext, "Stored partners", partners);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
