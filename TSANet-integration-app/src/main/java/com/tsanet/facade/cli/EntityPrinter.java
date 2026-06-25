package com.tsanet.facade.cli;

import static com.tsanet.facade.cli.TerminalColors.BLUE;
import static com.tsanet.facade.cli.TerminalColors.GREEN;
import static com.tsanet.facade.cli.TerminalColors.RED;
import static com.tsanet.facade.cli.TerminalColors.RESET;

import com.tsanet.api.connectapi.dto.CaseNoteDto;
import com.tsanet.api.connectapi.dto.CaseResponseDto;
import com.tsanet.api.connectapi.dto.PartnerSelectionDto;
import com.tsanet.api.connectapi.dto.UserContextDto;
import com.tsanet.api.connectapi.dto.WebhookSubscriptionDto;
import java.util.List;

public final class EntityPrinter {
    private EntityPrinter() {
    }

    public static void printNotes(CliRunContext context, String title, List<CaseNoteDto> notes) {
        if (notes.isEmpty()) {
            println(context, BLUE, "No notes.");
            return;
        }
        println(context, GREEN, title + " (" + notes.size() + "):");
        for (CaseNoteDto note : notes) {
            System.out.printf(
                " - id=%s caseToken=%s token=%s status=%s priority=%s summary=%s creator=%s%n",
                note.id(),
                note.caseToken(),
                note.token(),
                note.status(),
                note.priority(),
                note.summary(),
                note.creatorName()
            );
        }
    }

    public static void printResponses(CliRunContext context, String title, List<CaseResponseDto> responses) {
        if (responses.isEmpty()) {
            println(context, BLUE, "No responses.");
            return;
        }
        println(context, GREEN, title + " (" + responses.size() + "):");
        for (CaseResponseDto response : responses) {
            System.out.printf(
                " - id=%s caseToken=%s type=%s engineer=%s nextSteps=%s%n",
                response.id(),
                response.caseToken(),
                response.type(),
                response.engineerName(),
                response.nextSteps()
            );
        }
    }

    public static void printUserContext(CliRunContext context, String title, UserContextDto user) {
        println(context, GREEN, title + ":");
        System.out.printf(
            " - companyId=%s companyName=%s userId=%s username=%s email=%s name=%s %s%n",
            user.companyId(),
            user.companyName(),
            user.userId(),
            user.username(),
            user.email(),
            user.firstName(),
            user.lastName()
        );
    }

    public static void printWebhooks(CliRunContext context, String title, List<WebhookSubscriptionDto> subscriptions) {
        if (subscriptions.isEmpty()) {
            println(context, BLUE, "No webhook subscriptions.");
            return;
        }
        println(context, GREEN, title + " (" + subscriptions.size() + "):");
        for (WebhookSubscriptionDto subscription : subscriptions) {
            System.out.printf(
                " - id=%s active=%s events=%s url=%s%n",
                subscription.id(),
                subscription.active(),
                subscription.eventTypes(),
                subscription.callbackUrl()
            );
        }
    }

    public static void printPartners(CliRunContext context, String title, List<PartnerSelectionDto> partners) {
        if (partners.isEmpty()) {
            println(context, BLUE, "No partners.");
            return;
        }
        println(context, GREEN, title + " (" + partners.size() + "):");
        for (PartnerSelectionDto partner : partners) {
            System.out.printf(
                " - search=%s label=%s company=%s department=%s companyId=%s departmentId=%s documentId=%s%n",
                partner.searchTerm(),
                partner.label(),
                partner.companyName(),
                partner.departmentName(),
                partner.companyId(),
                partner.departmentId(),
                partner.documentId()
            );
        }
    }

    public static String error(CliRunContext context, String message) {
        if (context.isPlainOutput()) {
            return message;
        }
        return RED + message + RESET;
    }

    private static void println(CliRunContext context, String color, String message) {
        if (context.isPlainOutput()) {
            System.out.println(message);
            return;
        }
        System.out.println(color + message + RESET);
    }
}
