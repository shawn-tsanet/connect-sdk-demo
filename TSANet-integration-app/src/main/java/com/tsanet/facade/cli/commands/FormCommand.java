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
public class FormCommand implements Command {
    private final TsaNetApiSession session;
    private final CliRunContext cliRunContext;

    public FormCommand(TsaNetApiSession session, CliRunContext cliRunContext) {
        this.session = session;
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
            var form = session.collaborationRequests().getCreateForm(companyId);
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
