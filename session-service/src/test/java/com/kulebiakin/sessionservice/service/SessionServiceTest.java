package com.kulebiakin.sessionservice.service;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.entity.Session;
import com.kulebiakin.sessionservice.entity.SessionStatus;
import com.kulebiakin.sessionservice.entity.User;
import com.kulebiakin.sessionservice.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionRatingService sessionRatingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .userId(1L)
            .firstName("John")
            .lastName("Doe")
            .sessionsTaken(0)
            .build();

        testSession = Session.builder()
            .sessionId(1L)
            .sessionDateTime(LocalDateTime.now())
            .sessionStatus(SessionStatus.SCHEDULED)
            .coachId(1L)
            .user(testUser)
            .build();
    }

    @Test
    void getAllSessions_returnsListOfSessions() {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionRepository.findAll()).thenReturn(sessions);

        List<Session> result = sessionService.getAllSessions();

        assertThat(result).hasSize(1);
        verify(sessionRepository).findAll();
    }

    @Test
    void getSessionById_existingId_returnsSession() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        Session result = sessionService.getSessionById(1L);

        assertThat(result).isEqualTo(testSession);
        verify(sessionRepository).findById(1L);
    }

    @Test
    void getSessionById_nonExistingId_throwsException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSessionById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSessionsByCoachId_returnsFilteredSessions() {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionRepository.findByCoachId(1L)).thenReturn(sessions);

        List<Session> result = sessionService.getSessionsByCoachId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoachId()).isEqualTo(1L);
    }

    @Test
    void getSessionsByUserId_returnsFilteredSessions() {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionRepository.findByUserUserId(1L)).thenReturn(sessions);

        List<Session> result = sessionService.getSessionsByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getSessionsByStatus_returnsFilteredSessions() {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionRepository.findBySessionStatus(SessionStatus.SCHEDULED)).thenReturn(sessions);

        List<Session> result = sessionService.getSessionsByStatus(SessionStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessionStatus()).isEqualTo(SessionStatus.SCHEDULED);
    }

    @Test
    void createSession_savesAndReturnsSession() {
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session newSession = Session.builder()
            .sessionDateTime(LocalDateTime.now())
            .build();

        Session result = sessionService.createSession(1L, 1L, newSession);

        assertThat(result).isNotNull();
        verify(sessionRepository).save(newSession);
    }

    @Test
    void updateSession_existingId_updatesAndReturnsSession() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(1);
        Session updatedDetails = Session.builder()
            .sessionDateTime(newDateTime)
            .sessionStatus(SessionStatus.COMPLETED)
            .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        Session result = sessionService.updateSession(1L, updatedDetails);

        assertThat(result.getSessionDateTime()).isEqualTo(newDateTime);
        assertThat(result.getSessionStatus()).isEqualTo(SessionStatus.COMPLETED);
        verify(sessionRepository).save(testSession);
    }

    @Test
    void updateSession_nonExistingId_throwsException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.updateSession(99L, testSession))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSessionRating_delegatesToRatingService() {
        BigDecimal rating = BigDecimal.valueOf(8.5);
        String comment = "Great session!";
        when(sessionRatingService.updateSessionRating(1L, rating, comment)).thenReturn(testSession);

        Session result = sessionService.updateSessionRating(1L, rating, comment);

        assertThat(result).isNotNull();
        verify(sessionRatingService).updateSessionRating(1L, rating, comment);
    }

    @Test
    void processReview_delegatesToRatingService() {
        BigDecimal rating = BigDecimal.valueOf(9.0);
        String comment = "Excellent!";

        sessionService.processReview(1L, rating, comment);

        verify(sessionRatingService).updateSessionRating(1L, rating, comment);
    }

    @Test
    void deleteSession_existingId_deletesSession() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        sessionService.deleteSession(1L);

        verify(sessionRepository).delete(testSession);
    }

    @Test
    void deleteSession_nonExistingId_throwsException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.deleteSession(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
