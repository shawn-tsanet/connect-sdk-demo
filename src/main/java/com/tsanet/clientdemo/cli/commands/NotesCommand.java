package com.tsanet.clientdemo.cli.commands;

import com.tsanet.clientdemo.cli.CliArgs;
import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.cli.EntityPrinter;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import com.tsanet.clientdemo.connectapi.dto.CaseNoteDto;
import java.util.List;
import java.util.Scanner;
import org.springframework.stereotype.Component;

@Component
public class NotesCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public NotesCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
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
            return connectApiClient.getNotesForAllRequests();
        }
        return connectApiClient.getNotes(
            CliArgs.token(args).orElseThrow(() -> new IllegalArgumentException("Provide --token TOKEN or --all"))
        );
    }
}
