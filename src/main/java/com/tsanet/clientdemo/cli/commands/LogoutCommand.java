package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class LogoutCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public LogoutCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "logout";
    }

    @Override
    public String description() {
        return "Clear the current Connect API session";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        connectApiClient.logout();
        if (cliRunContext.isPlainOutput()) {
            System.out.println("logged out");
            return;
        }
        System.out.println("Connect API session cleared.");
    }
}
