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
public class ResponsesCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public ResponsesCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "responses";
    }

    @Override
    public String description() {
        return "Fetch case responses/comments from Connect API (--token TOKEN or --all)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            List<CaseResponseDto> responses = resolveResponses(args);
            EntityPrinter.printResponses(cliRunContext, "Case responses", responses);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }

    private List<CaseResponseDto> resolveResponses(String[] args) {
        if (CliArgs.hasFlag(args, "--all")) {
            return connectApiClient.getResponsesForAllRequests();
        }
        return connectApiClient.getResponses(
            CliArgs.token(args).orElseThrow(() -> new IllegalArgumentException("Provide --token TOKEN or --all"))
        );
    }
}
