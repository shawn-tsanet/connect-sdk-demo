package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliArgs;
import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.CollaborationRequestPrinter;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestStatusDto;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class StoredCollaborationRequestsCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public StoredCollaborationRequestsCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "stored-requests";
    }

    @Override
    public String description() {
        return "List collaboration requests stored in the local SQLite database";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            List<CollaborationRequestStatusDto> requests = CliArgs.companyId(args)
                .map(connectApiClient::getStoredCollaborationRequests)
                .orElseGet(connectApiClient::getStoredCollaborationRequests);

            CollaborationRequestPrinter.printList(cliRunContext, "Stored collaboration requests", requests);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
