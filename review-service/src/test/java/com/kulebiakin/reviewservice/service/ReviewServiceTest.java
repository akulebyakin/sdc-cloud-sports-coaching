package com.kulebiakin.reviewservice.service;

import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.reviewservice.dto.ReviewRequest;
import com.kulebiakin.reviewservice.messaging.ReviewMessageProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewMessageProducer messageProducer;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void submitReview_createsAndSendsReviewMessage() {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .coachId(1L)
            .build();

        ReviewMessage result = reviewService.submitReview(request);

        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(8.5));
        assertThat(result.getComment()).isEqualTo("Great session!");
        assertThat(result.getCoachId()).isEqualTo(1L);
        assertThat(result.getTimestamp()).isNotNull();
        verify(messageProducer).sendReviewMessage(any(ReviewMessage.class));
    }

    @Test
    void submitReview_setsTimestamp() {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .coachId(1L)
            .build();

        ReviewMessage result = reviewService.submitReview(request);

        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    void submitReview_passesAllFieldsCorrectly() {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(5L)
            .rating(BigDecimal.valueOf(9.5))
            .comment("Excellent coaching!")
            .coachId(3L)
            .build();

        ArgumentCaptor<ReviewMessage> captor = ArgumentCaptor.forClass(ReviewMessage.class);

        reviewService.submitReview(request);

        verify(messageProducer).sendReviewMessage(captor.capture());
        ReviewMessage captured = captor.getValue();

        assertThat(captured.getSessionId()).isEqualTo(5L);
        assertThat(captured.getRating()).isEqualTo(BigDecimal.valueOf(9.5));
        assertThat(captured.getComment()).isEqualTo("Excellent coaching!");
        assertThat(captured.getCoachId()).isEqualTo(3L);
    }

    @Test
    void submitReview_nullComment_setsNullComment() {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(6.0))
            .comment(null)
            .coachId(1L)
            .build();

        ReviewMessage result = reviewService.submitReview(request);

        assertThat(result.getComment()).isNull();
    }
}
