package com.kulebiakin.sportscoaching.controller;

import com.kulebiakin.sportscoaching.entity.Coach;
import com.kulebiakin.sportscoaching.entity.CoachStatus;
import com.kulebiakin.sportscoaching.service.CoachService;
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
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
@Slf4j
public class CoachController {

    private final CoachService coachService;

    @GetMapping
    public ResponseEntity<List<Coach>> getAllCoaches() {
        log.info("GET /api/coaches - Fetching all coaches");
        return ResponseEntity.ok(coachService.getAllCoaches());
    }

    @GetMapping("/{coachId}")
    public ResponseEntity<Coach> getCoachById(@PathVariable Long coachId) {
        log.info("GET /api/coaches/{} - Fetching coach by id", coachId);
        return ResponseEntity.ok(coachService.getCoachById(coachId));
    }

    @PostMapping
    public ResponseEntity<Coach> createCoach(@Valid @RequestBody Coach coach) {
        log.info("POST /api/coaches - Creating new coach");
        Coach createdCoach = coachService.createCoach(coach);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoach);
    }

    @PutMapping("/{coachId}")
    public ResponseEntity<Coach> updateCoach(@PathVariable Long coachId, @Valid @RequestBody Coach coach) {
        log.info("PUT /api/coaches/{} - Updating coach", coachId);
        return ResponseEntity.ok(coachService.updateCoach(coachId, coach));
    }

    @PostMapping("/rating")
    public ResponseEntity<Coach> updateCoachRating(@RequestBody Map<String, Object> payload) {
        Long coachId = Long.valueOf(payload.get("coachId").toString());
        BigDecimal rating = new BigDecimal(payload.get("rating").toString());
        Long totalSessions = Long.valueOf(payload.get("totalSessions").toString());

        log.info("POST /api/coaches/rating - Updating rating for coach {}", coachId);
        return ResponseEntity.ok(coachService.updateCoachRating(coachId, rating, totalSessions));
    }

    @PostMapping("/status")
    public ResponseEntity<Coach> updateCoachStatus(@RequestBody Map<String, Object> payload) {
        Long coachId = Long.valueOf(payload.get("coachId").toString());
        CoachStatus status = CoachStatus.valueOf(payload.get("status").toString().toUpperCase());

        log.info("POST /api/coaches/status - Updating status for coach {} to {}", coachId, status);
        return ResponseEntity.ok(coachService.updateCoachStatus(coachId, status));
    }

    @DeleteMapping("/{coachId}")
    public ResponseEntity<Void> deleteCoach(@PathVariable Long coachId) {
        log.info("DELETE /api/coaches/{} - Deleting coach", coachId);
        coachService.deleteCoach(coachId);
        return ResponseEntity.noContent().build();
    }
}
