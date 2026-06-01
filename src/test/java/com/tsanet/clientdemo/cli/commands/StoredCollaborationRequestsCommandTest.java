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

class StoredCollaborationRequestsCommandTest {
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
    void itPrintsStoredRequestsFromDatabase() {
        ConnectApiClient client = Mockito.mock(ConnectApiClient.class);
        when(client.getStoredCollaborationRequests()).thenReturn(
            List.of(new CollaborationRequestStatusDto(1L, "OPEN", "Cached", "Acme", 1L, "Beta", 2L, "tok1", null, null))
        );
        StoredCollaborationRequestsCommand command = new StoredCollaborationRequestsCommand(client, cliRunContext);

        command.execute(new String[0], new Scanner(""));

        assertThat(output.toString(StandardCharsets.UTF_8)).contains("Stored collaboration requests (1)");
        assertThat(output.toString(StandardCharsets.UTF_8)).contains("id=1");
    }
}
