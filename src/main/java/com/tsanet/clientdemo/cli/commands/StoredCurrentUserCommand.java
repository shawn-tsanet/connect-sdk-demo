package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class StoredCurrentUserCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public StoredCurrentUserCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "stored-me";
    }

    @Override
    public String description() {
        return "Show current user stored in SQLite";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            var users = connectApiClient.getStoredCurrentUser();
            if (users.isEmpty()) {
                System.out.println(EntityPrinter.error(cliRunContext, "No stored user context."));
                return;
            }
            EntityPrinter.printUserContext(cliRunContext, "Stored current user", users.getFirst());
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
