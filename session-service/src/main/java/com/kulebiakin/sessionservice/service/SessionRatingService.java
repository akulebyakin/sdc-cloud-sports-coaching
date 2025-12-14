package com.kulebiakin.sessionservice.service;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.client.CoachServiceClient;
import com.kulebiakin.sessionservice.entity.Session;
import com.kulebiakin.sessionservice.entity.SessionStatus;
import com.kulebiakin.sessionservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionRatingService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final CoachServiceClient coachServiceClient;

    @Transactional
    public Session updateSessionRating(Long sessionId, BigDecimal rating) {
        log.debug("Updating rating for session id: {} to {}", sessionId, rating);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));
        session.setRating(rating);
        session.setSessionStatus(SessionStatus.COMPLETED);

        Session savedSession = sessionRepository.save(session);

        // Update coach rating via REST call to CoachService
        Long coachId = session.getCoachId();
        Double avgRating = sessionRepository.getAverageRatingByCoachId(coachId);
        Long totalSessions = sessionRepository.countSessionsByCoachId(coachId);

        if (avgRating != null) {
            coachServiceClient.updateCoachRating(coachId, BigDecimal.valueOf(avgRating), totalSessions);
        }

        // Increment user sessions taken
        userService.incrementSessionsTaken(session.getUser().getUserId());

        return savedSession;
    }
}
