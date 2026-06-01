package com.tsanet.clientdemo.cli.commands;

import static com.tsanet.clientdemo.cli.TerminalColors.GREEN;
import static com.tsanet.clientdemo.cli.TerminalColors.RED;
import static com.tsanet.clientdemo.cli.TerminalColors.RESET;
import static com.tsanet.clientdemo.cli.TerminalColors.YELLOW;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class LoginCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public LoginCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String description() {
        return "Authenticate and store bearer token in session";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length >= 2) {
            login(args[0], args[1]);
            return;
        }

        if (connectApiClient.isAuthorized()) {
            String username = connectApiClient.currentUsername().orElse("unknown");
            println(YELLOW, "Already logged in as " + username);
            return;
        }

        System.out.print("Login: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        login(username, password);
    }

    private void login(String username, String password) {
        try {
            connectApiClient.login(username, password);
            if (cliRunContext.isPlainOutput()) {
                System.out.println("logged in as: " + username);
                return;
            }
            println(GREEN, "Login successful. Session ready.");
        } catch (Exception ex) {
            println(RED, "Login failed: " + ex.getMessage());
        }
    }

    private void println(String color, String message) {
        if (cliRunContext.isPlainOutput()) {
            System.out.println(message);
            return;
        }
        System.out.println(color + message + RESET);
    }
}
