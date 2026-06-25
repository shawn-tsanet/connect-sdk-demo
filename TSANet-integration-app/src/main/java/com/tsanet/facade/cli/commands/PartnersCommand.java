package com.tsanet.facade.cli.commands;

import com.tsanet.facade.cli.CliArgs;
import com.tsanet.facade.cli.CliRunContext;
import com.tsanet.facade.cli.EntityPrinter;
import com.tsanet.api.TsaNetApiSession;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class PartnersCommand implements Command {
    private final TsaNetApiSession session;
    private final CliRunContext cliRunContext;

    public PartnersCommand(TsaNetApiSession session, CliRunContext cliRunContext) {
        this.session = session;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "partners";
    }

    @Override
    public String description() {
        return "Search partners in Connect API (--search TERM)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            String searchTerm = CliArgs.search(args)
                .orElseThrow(() -> new IllegalArgumentException("Provide --search TERM"));
            EntityPrinter.printPartners(cliRunContext, "Partners", session.partners().searchPartners(searchTerm));
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
