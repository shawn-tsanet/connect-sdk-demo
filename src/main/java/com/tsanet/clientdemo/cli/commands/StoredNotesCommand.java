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
public class StoredNotesCommand implements Command {
    private final ConnectApiClient connectApiClient;
    private final CliRunContext cliRunContext;

    public StoredNotesCommand(ConnectApiClient connectApiClient, CliRunContext cliRunContext) {
        this.connectApiClient = connectApiClient;
        this.cliRunContext = cliRunContext;
    }

    @Override
    public String name() {
        return "stored-notes";
    }

    @Override
    public String description() {
        return "List notes stored in SQLite (--token TOKEN optional)";
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        try {
            List<CaseNoteDto> notes = CliArgs.token(args)
                .map(connectApiClient::getStoredNotes)
                .orElseGet(connectApiClient::getStoredNotes);
            EntityPrinter.printNotes(cliRunContext, "Stored notes", notes);
        } catch (Exception ex) {
            System.out.println(EntityPrinter.error(cliRunContext, "Failed: " + ex.getMessage()));
        }
    }
}
