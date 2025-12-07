package com.kulebiakin.sportscoaching.service;

import com.kulebiakin.sportscoaching.entity.Coach;
import com.kulebiakin.sportscoaching.entity.CoachStatus;
import com.kulebiakin.sportscoaching.repository.CoachRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachService {

    private final CoachRepository coachRepository;

    public List<Coach> getAllCoaches() {
        log.debug("Fetching all coaches");
        return coachRepository.findAll();
    }

    public Coach getCoachById(Long coachId) {
        log.debug("Fetching coach with id: {}", coachId);
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new EntityNotFoundException("Coach not found with id: " + coachId));
    }

    public List<Coach> getCoachesByStatus(CoachStatus status) {
        log.debug("Fetching coaches with status: {}", status);
        return coachRepository.findByCoachStatus(status);
    }

    @Transactional
    public Coach createCoach(Coach coach) {
        log.debug("Creating new coach: {} {}", coach.getFirstName(), coach.getLastName());
        return coachRepository.save(coach);
    }

    @Transactional
    public Coach updateCoach(Long coachId, Coach coachDetails) {
        log.debug("Updating coach with id: {}", coachId);
        Coach coach = getCoachById(coachId);
        coach.setFirstName(coachDetails.getFirstName());
        coach.setLastName(coachDetails.getLastName());
        return coachRepository.save(coach);
    }

    @Transactional
    public Coach updateCoachRating(Long coachId, BigDecimal newRating, Long totalSessions) {
        log.debug("Updating rating for coach id: {} to {} based on {} sessions", coachId, newRating, totalSessions);
        Coach coach = getCoachById(coachId);
        coach.setRating(newRating);

        if (newRating.compareTo(BigDecimal.valueOf(2)) < 0) {
            int newStrikeCount = coach.getStrikeCount() + 1;
            coach.setStrikeCount(newStrikeCount);
            log.info("Coach {} received a strike. Total strikes: {}", coachId, newStrikeCount);

            if (newStrikeCount >= 5) {
                coach.setCoachStatus(CoachStatus.DEACTIVATED);
                log.warn("Coach {} has been deactivated due to 5 strikes", coachId);
            }
        }

        return coachRepository.save(coach);
    }

    @Transactional
    public Coach updateCoachStatus(Long coachId, CoachStatus newStatus) {
        log.debug("Updating status for coach id: {} to {}", coachId, newStatus);
        Coach coach = getCoachById(coachId);
        coach.setCoachStatus(newStatus);
        return coachRepository.save(coach);
    }

    @Transactional
    public void deleteCoach(Long coachId) {
        log.debug("Deleting coach with id: {}", coachId);
        Coach coach = getCoachById(coachId);
        coachRepository.delete(coach);
    }
}
