package com.tsanet.facade.cli.commands;

import com.tsanet.facade.cli.CliArgs;
import com.tsanet.facade.cli.CliRunContext;
import com.tsanet.facade.cli.CollaborationRequestPrinter;
import com.tsanet.facade.cli.EntityPrinter;
import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.connectapi.dto.CollaborationRequestStatusDto;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class CollaborationRequestsCommand implements Command {
    private final TsaNetApiSession session;
    private final CliRunContext cliRunContext;

    public CollaborationRequestsCommand(TsaNetApiSession session, CliRunContext cliRunContext) {
        this.session = session;
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
            List<CollaborationRequestStatusDto> requests = session.collaborationRequests().listRequests();
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
