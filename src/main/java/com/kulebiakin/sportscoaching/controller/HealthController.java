package com.kulebiakin.sportscoaching.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("Health check endpoint called");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "sports-coaching-service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
