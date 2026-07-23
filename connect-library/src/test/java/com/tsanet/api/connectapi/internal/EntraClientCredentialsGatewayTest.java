package com.tsanet.api.connectapi.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tsanet.api.OAuthClientCredentials;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntraClientCredentialsGatewayTest {

    private HttpServer server;
    private final AtomicReference<String> receivedBody = new AtomicReference<>();
    private volatile int responseStatus = 200;
    private volatile String responseBody = "{\"access_token\":\"tok-123\",\"expires_in\":3599}";

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/token", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private OAuthClientCredentials credentials() {
        return OAuthClientCredentials.of(
            "http://localhost:" + server.getAddress().getPort() + "/token",
            "client-1",
            "secret-1",
            "audience-1/.default"
        );
    }

    @Test
    void itMintsATokenAndParsesExpiry() {
        var token = new EntraClientCredentialsGateway().fetchToken(credentials());

        assertThat(token.accessToken()).isEqualTo("tok-123");
        assertThat(token.expiresInSeconds()).isEqualTo(3599);
        assertThat(receivedBody.get())
            .contains("grant_type=client_credentials")
            .contains("client_id=client-1")
            .contains("client_secret=secret-1")
            .contains("scope=audience-1%2F.default");
    }

    @Test
    void itSurfacesTheIdpErrorCodeOnNon200() {
        responseStatus = 401;
        responseBody = "{\"error\":\"invalid_client\",\"error_description\":\"AADSTS7000215: bad secret\"}";

        assertThatThrownBy(() -> new EntraClientCredentialsGateway().fetchToken(credentials()))
            .hasMessageContaining("HTTP 401")
            .hasMessageContaining("invalid_client");
    }

    @Test
    void itRejectsAResponseWithoutAccessToken() {
        responseBody = "{\"token_type\":\"Bearer\"}";

        assertThatThrownBy(() -> new EntraClientCredentialsGateway().fetchToken(credentials()))
            .hasMessageContaining("access_token");
    }
}
