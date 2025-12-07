package com.kulebiakin.sportscoaching.service;

import com.kulebiakin.sportscoaching.entity.Coach;
import com.kulebiakin.sportscoaching.entity.Session;
import com.kulebiakin.sportscoaching.entity.SessionStatus;
import com.kulebiakin.sportscoaching.entity.User;
import com.kulebiakin.sportscoaching.repository.SessionRepository;
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
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final CoachService coachService;

    public List<Session> getAllSessions() {
        log.debug("Fetching all sessions");
        return sessionRepository.findAll();
    }

    public Session getSessionById(Long sessionId) {
        log.debug("Fetching session with id: {}", sessionId);
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with id: " + sessionId));
    }

    public List<Session> getSessionsByCoachId(Long coachId) {
        log.debug("Fetching sessions for coach id: {}", coachId);
        return sessionRepository.findByCoachCoachId(coachId);
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
        Coach coach = coachService.getCoachById(coachId);

        session.setUser(user);
        session.setCoach(coach);

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

    @Transactional
    public Session updateSessionRating(Long sessionId, BigDecimal rating) {
        log.debug("Updating rating for session id: {} to {}", sessionId, rating);
        Session session = getSessionById(sessionId);
        session.setRating(rating);
        session.setSessionStatus(SessionStatus.COMPLETED);

        Session savedSession = sessionRepository.save(session);

        // Update coach rating
        Long coachId = session.getCoach().getCoachId();
        Double avgRating = sessionRepository.getAverageRatingByCoachId(coachId);
        Long totalSessions = sessionRepository.countSessionsByCoachId(coachId);

        if (avgRating != null) {
            coachService.updateCoachRating(coachId, BigDecimal.valueOf(avgRating), totalSessions);
        }

        // Increment user sessions taken
        userService.incrementSessionsTaken(session.getUser().getUserId());

        return savedSession;
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        log.debug("Deleting session with id: {}", sessionId);
        Session session = getSessionById(sessionId);
        sessionRepository.delete(session);
    }
}
