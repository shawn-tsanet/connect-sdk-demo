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
public class CollaborationRequestsCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public CollaborationRequestsCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "requests";
    }

    @Override
    public String description() {
        return "Fetch collaboration requests from Connect API and store them locally";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            List<CollaborationRequestStatusDto> requests = connectApiClient.getCollaborationRequests();
            List<CollaborationRequestStatusDto> filtered = CliArgs.companyId(args)
                .map(companyId -> requests.stream()
                    .filter(request -> companyId.equals(request.submitCompanyId()) || companyId.equals(request.receiveCompanyId()))
                    .toList())
                .orElse(requests);

            CollaborationRequestPrinter.printList(cliRunContext, "Collaboration requests", filtered);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
