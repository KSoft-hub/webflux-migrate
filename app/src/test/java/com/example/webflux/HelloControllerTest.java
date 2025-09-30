package com.example.webflux;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class HelloControllerTest {

    private static final Object serverLock = new Object();
    private static volatile HttpServer mockServer;
    private static volatile int mockServerPort;
    private static volatile int externalStatus = 200;
    private static volatile String externalBody = "";

    @BeforeAll
    static void startMockServer() {
        startMockServerIfNecessary();
    }

    @AfterAll
    static void shutdownMockServer() {
        synchronized (serverLock) {
            if (mockServer != null) {
                mockServer.stop(0);
                mockServer = null;
            }
        }
    }

    @DynamicPropertySource
    static void registerExternalApiProperty(DynamicPropertyRegistry registry) {
        registry.add("external.api.url", () -> {
            startMockServerIfNecessary();
            return "http://localhost:" + mockServerPort + "/hello";
        });
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void helloEndpointReturnsExternalResponse() {
        externalStatus = 200;
        externalBody = "mocked-external-response";

        webTestClient.get()
                .uri("/hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("mocked-external-response");
    }

    @Test
    void helloEndpointWithIdReturnsGreeting() {
        webTestClient.get()
                .uri("/hello/42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("hello-42");
    }

    @Test
    void helloAdminEndpointReturnsGreeting() {
        webTestClient.get()
                .uri("/hello/admin/99")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("admin-hello-99");
    }

    private static void startMockServerIfNecessary() {
        if (mockServer != null) {
            return;
        }
        synchronized (serverLock) {
            if (mockServer != null) {
                return;
            }
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
                server.createContext("/hello", exchange -> {
                    byte[] responseBytes = externalBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(externalStatus, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    } finally {
                        exchange.close();
                    }
                });
                server.start();
                mockServer = server;
                mockServerPort = server.getAddress().getPort();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to start mock server", e);
            }
        }
    }
}
