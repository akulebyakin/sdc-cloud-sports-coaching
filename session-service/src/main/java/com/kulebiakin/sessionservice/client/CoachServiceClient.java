package com.kulebiakin.sessionservice.client;

import com.kulebiakin.common.dto.CoachRatingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@Slf4j
public class CoachServiceClient {

    private final WebClient webClient;

    public CoachServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${coach-service.url:http://localhost:8081}") String coachServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(coachServiceUrl).build();
    }

    public void updateCoachRating(Long coachId, BigDecimal rating, Long totalSessions) {
        log.info("Sending rating update to CoachService for coach {}: rating={}, sessions={}",
                coachId, rating, totalSessions);

        CoachRatingRequest request = CoachRatingRequest.builder()
                .coachId(coachId)
                .rating(rating)
                .totalSessions(totalSessions)
                .build();

        webClient.post()
                .uri("/api/coaches/rating")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully updated coach {} rating", coachId))
                .doOnError(e -> log.error("Failed to update coach {} rating: {}", coachId, e.getMessage()))
                .subscribe();
    }
}
