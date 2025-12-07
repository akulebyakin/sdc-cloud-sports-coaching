package com.kulebiakin.sportscoaching.controller;

import com.kulebiakin.sportscoaching.entity.Session;
import com.kulebiakin.sportscoaching.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<Session>> getAllSessions() {
        log.info("GET /api/sessions - Fetching all sessions");
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long sessionId) {
        log.info("GET /api/sessions/{} - Fetching session by id", sessionId);
        return ResponseEntity.ok(sessionService.getSessionById(sessionId));
    }

    @GetMapping("/coach/{coachId}")
    public ResponseEntity<List<Session>> getSessionsByCoachId(@PathVariable Long coachId) {
        log.info("GET /api/sessions/coach/{} - Fetching sessions by coach id", coachId);
        return ResponseEntity.ok(sessionService.getSessionsByCoachId(coachId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Session>> getSessionsByUserId(@PathVariable Long userId) {
        log.info("GET /api/sessions/user/{} - Fetching sessions by user id", userId);
        return ResponseEntity.ok(sessionService.getSessionsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Session> createSession(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long coachId = Long.valueOf(payload.get("coachId").toString());

        Session session = new Session();
        session.setSessionDateTime(java.time.LocalDateTime.parse(payload.get("sessionDateTime").toString()));

        log.info("POST /api/sessions - Creating new session for user {} with coach {}", userId, coachId);
        Session createdSession = sessionService.createSession(userId, coachId, session);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<Session> updateSession(@PathVariable Long sessionId, @Valid @RequestBody Session session) {
        log.info("PUT /api/sessions/{} - Updating session", sessionId);
        return ResponseEntity.ok(sessionService.updateSession(sessionId, session));
    }

    @PostMapping("/{sessionId}/rating")
    public ResponseEntity<Session> updateSessionRating(@PathVariable Long sessionId, @RequestBody Map<String, Object> payload) {
        BigDecimal rating = new BigDecimal(payload.get("rating").toString());
        log.info("POST /api/sessions/{}/rating - Updating session rating to {}", sessionId, rating);
        return ResponseEntity.ok(sessionService.updateSessionRating(sessionId, rating));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        log.info("DELETE /api/sessions/{} - Deleting session", sessionId);
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
