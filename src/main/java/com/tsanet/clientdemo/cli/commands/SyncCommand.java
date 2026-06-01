package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class SyncCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public SyncCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "sync";
    }

    @Override
    public String description() {
        return "Fetch collaboration requests, notes, and responses for all requests";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            connectApiClient.syncAllRequestDetails();
            System.out.println("Sync completed.");
            EntityPrinter.printNotes(cliRunContext, "Stored notes", connectApiClient.getStoredNotes());
            EntityPrinter.printResponses(cliRunContext, "Stored case responses", connectApiClient.getStoredResponses());
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
