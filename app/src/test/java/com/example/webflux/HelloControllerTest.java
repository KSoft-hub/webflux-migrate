package com.example.webflux;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest {

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void startMockServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownMockServer() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @DynamicPropertySource
    static void registerExternalApiProperty(DynamicPropertyRegistry registry) {
        registry.add("external.api.url", () -> mockWebServer.url("/hello").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloEndpointReturnsExternalResponse() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("mocked-external-response"));

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Filtered", "true"))
                .andExpect(content().string("mocked-external-response"));
    }

    @Test
    void helloEndpointWithIdReturnsGreeting() throws Exception {
        mockMvc.perform(get("/hello/42"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Filtered", "true"))
                .andExpect(content().string("hello-42"));
    }

    @Test
    void helloAdminEndpointReturnsGreeting() throws Exception {
        mockMvc.perform(get("/hello/admin/99"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Filtered", "true"))
                .andExpect(content().string("admin-hello-99"));
    }
}
