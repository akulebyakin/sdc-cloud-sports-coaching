package com.kulebiakin.reviewservice.service;

import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.reviewservice.dto.ReviewRequest;
import com.kulebiakin.reviewservice.messaging.ReviewMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewMessageProducer messageProducer;

    public ReviewMessage submitReview(ReviewRequest request) {
        log.info("Submitting review for session {}: rating={}", request.getSessionId(), request.getRating());

        // Build the review message with timestamp
        ReviewMessage reviewMessage = ReviewMessage.builder()
                .sessionId(request.getSessionId())
                .rating(request.getRating())
                .comment(request.getComment())
                .coachId(request.getCoachId())
                .timestamp(LocalDateTime.now())
                .build();

        // Send to Azure Service Bus
        messageProducer.sendReviewMessage(reviewMessage);

        log.info("Review submitted successfully for session {}", request.getSessionId());
        return reviewMessage;
    }
}
