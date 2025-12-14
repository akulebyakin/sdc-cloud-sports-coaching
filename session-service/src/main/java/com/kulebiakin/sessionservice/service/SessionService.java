package com.kulebiakin.sessionservice.service;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.entity.Session;
import com.kulebiakin.sessionservice.entity.SessionStatus;
import com.kulebiakin.sessionservice.entity.User;
import com.kulebiakin.sessionservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionRatingService sessionRatingService;
    private final UserService userService;

    public List<Session> getAllSessions() {
        log.debug("Fetching all sessions");
        return sessionRepository.findAll();
    }

    public Session getSessionById(Long sessionId) {
        log.debug("Fetching session with id: {}", sessionId);
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));
    }

    public List<Session> getSessionsByCoachId(Long coachId) {
        log.debug("Fetching sessions for coach id: {}", coachId);
        return sessionRepository.findByCoachId(coachId);
    }

    public List<Session> getSessionsByUserId(Long userId) {
        log.debug("Fetching sessions for user id: {}", userId);
        return sessionRepository.findByUserUserId(userId);
    }

    public List<Session> getSessionsByStatus(SessionStatus status) {
        log.debug("Fetching sessions with status: {}", status);
        return sessionRepository.findBySessionStatus(status);
    }

    @Transactional
    public Session createSession(Long userId, Long coachId, Session session) {
        log.debug("Creating new session for user {} with coach {}", userId, coachId);
        User user = userService.getUserById(userId);
        session.setUser(user);
        session.setCoachId(coachId);
        return sessionRepository.save(session);
    }

    @Transactional
    public Session updateSession(Long sessionId, Session sessionDetails) {
        log.debug("Updating session with id: {}", sessionId);
        Session session = getSessionById(sessionId);
        session.setSessionDateTime(sessionDetails.getSessionDateTime());
        session.setSessionStatus(sessionDetails.getSessionStatus());
        return sessionRepository.save(session);
    }

    public Session updateSessionRating(Long sessionId, BigDecimal rating) {
        return sessionRatingService.updateSessionRating(sessionId, rating);
    }

    /**
     * Process a review message received from ReviewService via Azure Service Bus.
     * Updates the session rating and notifies CoachService.
     */
    public void processReview(Long sessionId, BigDecimal rating) {
        log.info("Processing review for session {}: rating={}", sessionId, rating);
        sessionRatingService.updateSessionRating(sessionId, rating);
        log.info("Successfully processed review for session {}", sessionId);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        log.debug("Deleting session with id: {}", sessionId);
        Session session = getSessionById(sessionId);
        sessionRepository.delete(session);
    }
}
