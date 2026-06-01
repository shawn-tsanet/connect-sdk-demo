package com.tsanet.clientdemo.cli.commands;

import static com.tsanet.clientdemo.cli.TerminalColors.GREEN;
import static com.tsanet.clientdemo.cli.TerminalColors.RESET;
import static com.tsanet.clientdemo.cli.TerminalColors.YELLOW;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class SessionStatusCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public SessionStatusCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "session";
    }

    @Override
    public String description() {
        return "Show current authentication state";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (connectApiClient.isAuthorized()) {
            String username = connectApiClient.currentUsername().orElse("unknown");
            println(GREEN, "Logged in as: " + username);
            if (cliRunContext.isPlainOutput()) {
                System.out.println("authorized: true");
                System.out.println("username: " + username);
            }
            return;
        }
        println(YELLOW, "Not logged in");
        if (cliRunContext.isPlainOutput()) {
            System.out.println("authorized: false");
        }
    }

    private void println(String color, String message) {
        if (cliRunContext.isPlainOutput()) {
            return;
        }
        System.out.println(color + message + RESET);
    }
}
