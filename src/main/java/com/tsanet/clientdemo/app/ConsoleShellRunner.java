package com.tsanet.clientdemo.app;

import static com.tsanet.clientdemo.cli.TerminalColors.BLUE;
import static com.tsanet.clientdemo.cli.TerminalColors.GREEN;
import static com.tsanet.clientdemo.cli.TerminalColors.RED;
import static com.tsanet.clientdemo.cli.TerminalColors.RESET;
import static com.tsanet.clientdemo.cli.TerminalColors.YELLOW;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.commands.Command;
import com.tsanet.clientdemo.cli.commands.CommandRegistry;
import com.tsanet.clientdemo.cli.commands.ExitSignal;
import com.tsanet.clientdemo.config.AuthProperties;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConsoleShellRunner implements CommandLineRunner {
    private final CommandRegistry commandRegistry;
    private final AuthProperties authProperties;
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public ConsoleShellRunner(
        CommandRegistry commandRegistry,
        AuthProperties authProperties,
        ConnectApiClient connectApiClient,
        CliRunContext cliRunContext
    ) {
        this.commandRegistry = commandRegistry;
        this.authProperties = authProperties;
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public void run(String... args) {
        BatchArgs batchArgs = parseBatchArgs(args);
        if (batchArgs.enabled()) {
            cliRunContext.configure(true, batchArgs.plainOutput());
            runBatch(batchArgs.commandLine());
            return;
        }

        cliRunContext.configure(false, false);
        println(BLUE, "TSANet Client Demo started. Type 'help' for commands.");

        try (Scanner scanner = new Scanner(System.in)) {
            //tryAutoLogin();
            loop(scanner);
        }
    }

    private void runBatch(String commandLine) {
        tryAutoLogin();
        if (commandLine.isBlank()) {
            println(RED, "Batch mode requires a command. Example: --batch requests --company-id 1");
            System.exit(1);
        }

        String[] parts = commandLine.trim().split("\\s+");
        String commandName = parts[0];
        String[] commandArgs = Arrays.copyOfRange(parts, 1, parts.length, String[].class);

        Command command = commandRegistry.find(commandName).orElse(null);
        if (command == null) {
            println(RED, "Unknown command: " + commandName);
            System.exit(1);
        }

        command.execute(commandArgs, new Scanner(""));
    }

    private void tryAutoLogin() {
        if (connectApiClient.isAuthorized()) {
            return;
        }

        if (authProperties.isConfigured()) {
            try {
                connectApiClient.login(authProperties.username(), authProperties.password());
                println(GREEN, "Auto login succeeded. Ready to consume commands.");
                return;
            } catch (Exception ex) {
                if (cliRunContext.isPlainOutput()) {
                    System.err.println("Auto login failed: " + ex.getMessage());
                } else {
                    println(YELLOW, "Auto login attempted with configured properties, but failed.");
                    println(RED, "Current state: UNAUTHORIZED");
                    println(BLUE, "Use 'login' command to authenticate interactively.");
                }
                return;
            }
        }

        if (!cliRunContext.isPlainOutput()) {
            println(YELLOW, "Credentials are not fully configured in properties.");
            println(RED, "Current state: UNAUTHORIZED");
            println(BLUE, "Use 'login' command to authenticate interactively.");
        }
    }

    private void loop(Scanner scanner) {
        while (true) {
            System.out.print(BLUE + "tsa> " + RESET);
            if (!scanner.hasNextLine()) {
                System.out.println();
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String commandName = parts[0];
            String[] commandArgs = Arrays.copyOfRange(parts, 1, parts.length);

            try {
                Command command = commandRegistry.find(commandName).orElse(null);
                if (command == null) {
                    println(RED, "Unknown command: " + commandName);
                    continue;
                }
                command.execute(commandArgs, scanner);
            } catch (ExitSignal ignored) {
                break;
            } catch (Exception ex) {
                println(RED, "Command failed: " + ex.getMessage());
            }
        }
    }

    private void println(String color, String message) {
        if (cliRunContext.isPlainOutput()) {
            System.out.println(message);
            return;
        }
        System.out.println(color + message + RESET);
    }

    private static BatchArgs parseBatchArgs(String[] args) {
        List<String> remaining = new ArrayList<>(Arrays.asList(args));
        boolean batchMode = false;
        boolean plainOutput = false;

        for (int i = 0; i < remaining.size(); i++) {
            String arg = remaining.get(i);
            if ("--batch".equals(arg)) {
                batchMode = true;
                remaining.remove(i);
                i--;
                continue;
            }
            if ("--plain".equals(arg)) {
                plainOutput = true;
                remaining.remove(i);
                i--;
            }
        }

        return new BatchArgs(batchMode, plainOutput, String.join(" ", remaining));
    }

    private record BatchArgs(boolean enabled, boolean plainOutput, String commandLine) {
    }
}
