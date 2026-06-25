package com.tsanet.facade.cli.commands;

import com.tsanet.facade.cli.CliArgs;
import com.tsanet.facade.cli.CliRunContext;
import com.tsanet.facade.cli.CollaborationRequestPrinter;
import com.tsanet.facade.cli.EntityPrinter;
import com.tsanet.api.TsaNetApiSession;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class CreateRequestCommand implements Command {
    private final TsaNetApiSession session;
    private final CliRunContext cliRunContext;

    public CreateRequestCommand(TsaNetApiSession session, CliRunContext cliRunContext) {
        this.session = session;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "create-request";
    }

    @Override
    public String description() {
        return "Create a collaboration request for another company";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            long receiverCompanyId = CliArgs.companyId(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --company-id ID"));
            String caseNumber = CliArgs.caseNumber(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --case-number VALUE"));
            String summary = CliArgs.summary(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --summary VALUE"));
            String description = CliArgs.description(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --description VALUE"));

            var created = session.collaborationRequests().createRequest(
                receiverCompanyId,
                caseNumber,
                summary,
                description
            );
            CollaborationRequestPrinter.printList(cliRunContext, "Created collaboration request", List.of(created));
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
