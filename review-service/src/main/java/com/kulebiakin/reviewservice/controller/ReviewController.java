package com.kulebiakin.reviewservice.controller;

import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.reviewservice.dto.ReviewRequest;
import com.kulebiakin.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Management", description = "Endpoints for submitting session reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a session review")
    public ResponseEntity<ReviewMessage> submitReview(@Valid @RequestBody ReviewRequest request) {
        log.info("POST /api/reviews - Submitting review for session {}", request.getSessionId());
        ReviewMessage result = reviewService.submitReview(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }
}
