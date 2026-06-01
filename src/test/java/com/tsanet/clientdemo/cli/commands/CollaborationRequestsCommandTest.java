package com.tsanet.clientdemo.cli.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tsanet.clientdemo.cli.CliRunContext;
import com.tsanet.clientdemo.connectapi.ConnectApiClient;
import com.tsanet.clientdemo.connectapi.dto.CollaborationRequestStatusDto;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CollaborationRequestsCommandTest {
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;
    private CliRunContext cliRunContext;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        cliRunContext = new CliRunContext();
        cliRunContext.configure(true, true);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void itPrintsCollaborationRequestsWhenApiReturnsData() {
        ConnectApiClient client = Mockito.mock(ConnectApiClient.class);
        when(client.getCollaborationRequests()).thenReturn(
            List.of(
                new CollaborationRequestStatusDto(
                    1L,
                    "OPEN",
                    "Need help",
                    "Acme",
                    10L,
                    "Beta",
                    20L,
                    "tok1",
                    "2026-01-01T00:00:00Z",
                    "2026-01-02T00:00:00Z"
                ),
                new CollaborationRequestStatusDto(
                    2L,
                    "CLOSED",
                    "Done",
                    "Acme",
                    10L,
                    "Gamma",
                    30L,
                    "tok2",
                    "2026-01-03T00:00:00Z",
                    "2026-01-04T00:00:00Z"
                )
            )
        );
        CollaborationRequestsCommand command = new CollaborationRequestsCommand(client, cliRunContext);

        command.execute(new String[0], new Scanner(""));

        String printed = output.toString(StandardCharsets.UTF_8);
        assertThat(printed).contains("Collaboration requests (2)");
        assertThat(printed).contains("id=1");
        assertThat(printed).contains("status=OPEN");
        assertThat(printed).contains("from=Acme");
        assertThat(printed).contains("to=Beta");
        assertThat(printed).contains("summary=Need help");
    }

    @Test
    void itFiltersRequestsByCompanyId() {
        ConnectApiClient client = Mockito.mock(ConnectApiClient.class);
        when(client.getCollaborationRequests()).thenReturn(
            List.of(
                new CollaborationRequestStatusDto(1L, "OPEN", "A", "Acme", 1L, "Beta", 2L, "tok1", null, null),
                new CollaborationRequestStatusDto(2L, "OPEN", "B", "Acme", 3L, "Gamma", 4L, "tok2", null, null)
            )
        );
        CollaborationRequestsCommand command = new CollaborationRequestsCommand(client, cliRunContext);

        command.execute(new String[] {"--company-id", "1"}, new Scanner(""));

        String printed = output.toString(StandardCharsets.UTF_8);
        assertThat(printed).contains("Collaboration requests (1)");
        assertThat(printed).contains("id=1");
        assertThat(printed).doesNotContain("id=2");
    }

    @Test
    void itPrintsEmptyMessageWhenApiReturnsNoRequests() {
        ConnectApiClient client = Mockito.mock(ConnectApiClient.class);
        when(client.getCollaborationRequests()).thenReturn(List.of());
        CollaborationRequestsCommand command = new CollaborationRequestsCommand(client, cliRunContext);

        command.execute(new String[0], new Scanner(""));

        assertThat(output.toString(StandardCharsets.UTF_8)).contains("No collaboration requests.");
    }

    @Test
    void itPrintsFailureWhenNotLoggedInThrows() {
        ConnectApiClient client = Mockito.mock(ConnectApiClient.class);
        when(client.getCollaborationRequests()).thenThrow(new IllegalStateException("Not logged in"));
        CollaborationRequestsCommand command = new CollaborationRequestsCommand(client, cliRunContext);

        command.execute(new String[0], new Scanner(""));

        assertThat(output.toString(StandardCharsets.UTF_8)).contains("Failed: Not logged in");
    }
}
