package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliArgs;
import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class PartnersCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public PartnersCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "partners";
    }

    @Override
    public String description() {
        return "Search partners in Connect API (--search TERM)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            String searchTerm = CliArgs.search(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --search TERM"));
            EntityPrinter.printPartners(cliRunContext, "Partners", connectApiClient.searchPartners(searchTerm));
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
