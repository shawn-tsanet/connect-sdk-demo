package com.tsanet.facade.cli;

import static com.tsanet.facade.cli.TerminalColors.BLUE;
import static com.tsanet.facade.cli.TerminalColors.GREEN;
import static com.tsanet.facade.cli.TerminalColors.RESET;

import com.tsanet.api.connectapi.dto.CollaborationRequestStatusDto;
import java.util.List;

public final class CollaborationRequestPrinter {
    private CollaborationRequestPrinter() {
    }

    public static void printList(CliRunContext cliRunContext, String title, List<CollaborationRequestStatusDto> requests) {
        if (requests.isEmpty()) {
            println(cliRunContext, BLUE, "No collaboration requests.");
            return;
        }

        println(cliRunContext, GREEN, title + " (" + requests.size() + "):");
        for (CollaborationRequestStatusDto request : requests) {
            System.out.printf(
                " - id=%s status=%s token=%s submitCompanyId=%s receiveCompanyId=%s from=%s to=%s summary=%s%n",
                request.id(),
                request.status(),
                request.token(),
                request.submitCompanyId(),
                request.receiveCompanyId(),
                request.submitCompanyName(),
                request.receiveCompanyName(),
                request.summary()
            );
        }
    }

    private static void println(CliRunContext cliRunContext, String color, String message) {
        if (cliRunContext.isPlainOutput()) {
            System.out.println(message);
            return;
        }
        System.out.println(color + message + RESET);
    }
}
