package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliArgs;
import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import com.tsanet.clientdemo.connectapi.dto.CaseResponseDto;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class StoredResponsesCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public StoredResponsesCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "stored-responses";
    }

    @Override
    public String description() {
        return "List case responses stored in SQLite (--token TOKEN optional)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            List<CaseResponseDto> responses = CliArgs.token(args)
                .map(connectApiClient::getStoredResponses)
                .orElseGet(connectApiClient::getStoredResponses);
            EntityPrinter.printResponses(cliRunContext, "Stored case responses", responses);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
