package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliArgs;
import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.CollaborationRequestPrinter;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class FormCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public FormCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "form";
    }

    @Override
    public String description() {
        return "Fetch collaboration request form template for a receiver company (--company-id ID)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            long companyId = CliArgs.companyId(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --company-id ID"));
            var form = connectApiClient.getCollaborationRequestForm(companyId);
            System.out.printf(
                "receiverCompanyId=%s documentId=%s customFieldCount=%s%n",
                form.receiverCompanyId(),
                form.documentId(),
                form.customFieldCount()
            );
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
