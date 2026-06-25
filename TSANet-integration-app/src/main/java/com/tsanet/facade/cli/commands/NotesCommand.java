package com.tsanet.facade.cli.commands;

import com.tsanet.facade.cli.CliArgs;
import com.tsanet.facade.cli.CliRunContext;
import com.tsanet.facade.cli.EntityPrinter;
import com.tsanet.api.TsaNetApiSession;
import com.tsanet.api.connectapi.dto.CaseNoteDto;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class NotesCommand implements Command {
    private final TsaNetApiSession session;
    private final CliRunContext cliRunContext;

    public NotesCommand(TsaNetApiSession session, CliRunContext cliRunContext) {
        this.session = session;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "notes";
    }

    @Override
    public String description() {
        return "Fetch notes from Connect API (--token TOKEN or --all)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            List<CaseNoteDto> notes = resolveNotes(args);
            EntityPrinter.printNotes(cliRunContext, "Notes", notes);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }

    private List<CaseNoteDto> resolveNotes(String[] args) {
        if (CliArgs.hasFlag(args, "--all")) {
            return session.caseNotes().listNotesForAllRequests();
        }
        return session.caseNotes().listNotesForRequest(
            CliArgs.token(args).orElseThrow(() -> new IllegalArgumentException("Provide --token TOKEN or --all"))
        );
    }
}
