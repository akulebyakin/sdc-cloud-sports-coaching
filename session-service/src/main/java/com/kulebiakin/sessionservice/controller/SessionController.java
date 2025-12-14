package com.kulebiakin.sessionservice.controller;

import com.kulebiakin.sessionservice.entity.Session;
import com.kulebiakin.sessionservice.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Management", description = "Endpoints for managing coaching sessions")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "Get all sessions")
    public ResponseEntity<List<Session>> getAllSessions() {
        log.info("GET /api/sessions - Fetching all sessions");
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session by ID")
    public ResponseEntity<Session> getSessionById(@PathVariable Long sessionId) {
        log.info("GET /api/sessions/{} - Fetching session by id", sessionId);
        return ResponseEntity.ok(sessionService.getSessionById(sessionId));
    }

    @GetMapping("/coach/{coachId}")
    @Operation(summary = "Get sessions by coach ID")
    public ResponseEntity<List<Session>> getSessionsByCoachId(@PathVariable Long coachId) {
        log.info("GET /api/sessions/coach/{} - Fetching sessions by coach id", coachId);
        return ResponseEntity.ok(sessionService.getSessionsByCoachId(coachId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get sessions by user ID")
    public ResponseEntity<List<Session>> getSessionsByUserId(@PathVariable Long userId) {
        log.info("GET /api/sessions/user/{} - Fetching sessions by user id", userId);
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }

    @PostMapping
    @Operation(summary = "Create a new session")
    public ResponseEntity<Session> createSession(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long coachId = Long.valueOf(payload.get("coachId").toString());

        Session session = new Session();
        session.setSessionDateTime(LocalDateTime.parse(payload.get("sessionDateTime").toString()));

        log.info("POST /api/sessions - Creating new session for user {} with coach {}", userId, coachId);
        Session createdSession = sessionService.createSession(userId, coachId, session);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update a session")
    public ResponseEntity<Session> updateSession(@PathVariable Long sessionId, @Valid @RequestBody Session session) {
        log.info("PUT /api/sessions/{} - Updating session", sessionId);
        return ResponseEntity.ok(sessionService.updateSession(sessionId, session));
    }

    @PostMapping("/{sessionId}/rating")
    @Operation(summary = "Rate a completed session")
    public ResponseEntity<Session> updateSessionRating(@PathVariable Long sessionId, @RequestBody Map<String, Object> payload) {
        BigDecimal rating = new BigDecimal(payload.get("rating").toString());
        log.info("POST /api/sessions/{}/rating - Updating session rating to {}", sessionId, rating);
        return ResponseEntity.ok(sessionService.updateSessionRating(sessionId, rating));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a session")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        log.info("DELETE /api/sessions/{} - Deleting session", sessionId);
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
