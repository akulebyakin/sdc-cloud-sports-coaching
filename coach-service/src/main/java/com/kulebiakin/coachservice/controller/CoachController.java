package com.kulebiakin.coachservice.controller;

import com.kulebiakin.coachservice.entity.Coach;
import com.kulebiakin.coachservice.entity.CoachStatus;
import com.kulebiakin.coachservice.service.CoachService;
import com.kulebiakin.common.dto.CoachRatingRequest;
import com.kulebiakin.common.dto.CoachStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coach Management", description = "Endpoints for managing coaches")
public class CoachController {

    private final CoachService coachService;

    @GetMapping
    @Operation(summary = "Get all coaches")
    public ResponseEntity<List<Coach>> getAllCoaches() {
        log.info("GET /api/coaches - Fetching all coaches");
        return ResponseEntity.ok(coachService.getAllCoaches());
    }

    @GetMapping("/{coachId}")
    @Operation(summary = "Get coach by ID")
    public ResponseEntity<Coach> getCoachById(@PathVariable Long coachId) {
        log.info("GET /api/coaches/{} - Fetching coach by id", coachId);
        return ResponseEntity.ok(coachService.getCoachById(coachId));
    }

    @PostMapping
    @Operation(summary = "Create a new coach")
    public ResponseEntity<Coach> createCoach(@Valid @RequestBody Coach coach) {
        log.info("POST /api/coaches - Creating new coach");
        Coach createdCoach = coachService.createCoach(coach);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoach);
    }

    @PutMapping("/{coachId}")
    @Operation(summary = "Update an existing coach")
    public ResponseEntity<Coach> updateCoach(@PathVariable Long coachId, @Valid @RequestBody Coach coach) {
        log.info("PUT /api/coaches/{} - Updating coach", coachId);
        return ResponseEntity.ok(coachService.updateCoach(coachId, coach));
    }

    @PostMapping("/rating")
    @Operation(summary = "Update coach rating based on session feedback")
    public ResponseEntity<Coach> updateCoachRating(@Valid @RequestBody CoachRatingRequest request) {
        log.info("POST /api/coaches/rating - Updating rating for coach {}", request.getCoachId());
        return ResponseEntity.ok(coachService.updateCoachRating(
                request.getCoachId(),
                request.getRating(),
                request.getTotalSessions()
        ));
    }

    @PostMapping("/status")
    @Operation(summary = "Update coach status (activate/deactivate)")
    public ResponseEntity<Coach> updateCoachStatus(@Valid @RequestBody CoachStatusRequest request) {
        log.info("POST /api/coaches/status - Updating status for coach {} to {}",
                request.getCoachId(), request.getStatus());
        CoachStatus status = CoachStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(coachService.updateCoachStatus(request.getCoachId(), status));
    }

    @DeleteMapping("/{coachId}")
    @Operation(summary = "Delete a coach")
    public ResponseEntity<Void> deleteCoach(@PathVariable Long coachId) {
        log.info("DELETE /api/coaches/{} - Deleting coach", coachId);
        coachService.deleteCoach(coachId);
        return ResponseEntity.noContent().build();
    }
}
