package com.kulebiakin.sessionservice.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CoachServiceClientTest {

    private MockWebServer mockWebServer;
    private CoachServiceClient coachServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        coachServiceClient = new CoachServiceClient(WebClient.builder(), baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void updateCoachRating_sendsCorrectRequest() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"));

        coachServiceClient.updateCoachRating(1L, BigDecimal.valueOf(8.5), 10L);

        // Wait for async request to be sent
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);

        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/coaches/rating");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Content-Type")).contains("application/json");

        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"coachId\":1");
        assertThat(body).contains("\"rating\":8.5");
        assertThat(body).contains("\"totalSessions\":10");
    }

    @Test
    void updateCoachRating_handlesServerError() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("{\"error\": \"Internal Server Error\"}"));

        // Should not throw exception - errors are logged
        coachServiceClient.updateCoachRating(1L, BigDecimal.valueOf(7.0), 5L);

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void updateCoachRating_handlesConnectionTimeout() throws InterruptedException {
        // Don't enqueue any response - simulates timeout
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBodyDelay(10, TimeUnit.SECONDS));

        // Should not throw - async operation
        coachServiceClient.updateCoachRating(2L, BigDecimal.valueOf(9.0), 15L);

        // Request should still be sent
        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
    }

    @Test
    void updateCoachRating_sendsCorrectPayloadStructure() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        BigDecimal rating = BigDecimal.valueOf(7.25);
        Long totalSessions = 25L;
        Long coachId = 42L;

        coachServiceClient.updateCoachRating(coachId, rating, totalSessions);

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"coachId\":42");
        assertThat(body).contains("\"rating\":7.25");
        assertThat(body).contains("\"totalSessions\":25");
    }

    @Test
    void updateCoachRating_handlesZeroRating() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        coachServiceClient.updateCoachRating(1L, BigDecimal.ZERO, 0L);

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"rating\":0");
        assertThat(body).contains("\"totalSessions\":0");
    }

    @Test
    void updateCoachRating_handlesMaxRating() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        coachServiceClient.updateCoachRating(1L, BigDecimal.TEN, 100L);

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"rating\":10");
    }
}
