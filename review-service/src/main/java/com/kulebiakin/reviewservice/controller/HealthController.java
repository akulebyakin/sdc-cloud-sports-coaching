package com.kulebiakin.reviewservice.controller;

import com.kulebiakin.common.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.builder()
                .status("UP")
                .service("review-service")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
