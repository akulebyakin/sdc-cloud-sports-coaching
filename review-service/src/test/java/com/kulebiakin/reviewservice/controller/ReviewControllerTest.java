package com.kulebiakin.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.reviewservice.dto.ReviewRequest;
import com.kulebiakin.reviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @Test
    void submitReview_validRequest_returnsAccepted() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .coachId(1L)
            .build();

        ReviewMessage response = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        when(reviewService.submitReview(any(ReviewRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sessionId").value(1))
            .andExpect(jsonPath("$.rating").value(8.5));
    }

    @Test
    void submitReview_missingSessionId_returnsBadRequest() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .rating(BigDecimal.valueOf(8.5))
            .coachId(1L)
            .build();

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_missingRating_returnsBadRequest() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .coachId(1L)
            .build();

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_missingCoachId_returnsBadRequest() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .build();

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_ratingTooHigh_returnsBadRequest() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(11.0))
            .coachId(1L)
            .build();

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_ratingTooLow_returnsBadRequest() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(-1.0))
            .coachId(1L)
            .build();

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_optionalCommentNull_returnsAccepted() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .comment(null)
            .coachId(1L)
            .build();

        ReviewMessage response = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        when(reviewService.submitReview(any(ReviewRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());
    }
}
