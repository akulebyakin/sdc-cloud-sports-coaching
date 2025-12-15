package com.kulebiakin.sessionservice.service;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.client.CoachServiceClient;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionRatingServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserService userService;

    @Mock
    private CoachServiceClient coachServiceClient;

    @InjectMocks
    private SessionRatingService sessionRatingService;

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
    void updateSessionRating_updatesRatingAndStatus() {
        BigDecimal rating = BigDecimal.valueOf(8.5);
        String comment = "Great session!";

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
        when(sessionRepository.getAverageRatingByCoachId(1L)).thenReturn(8.5);
        when(sessionRepository.countSessionsByCoachId(1L)).thenReturn(5L);
        when(userService.incrementSessionsTaken(1L)).thenReturn(testUser);

        Session result = sessionRatingService.updateSessionRating(1L, rating, comment);

        assertThat(result.getRating()).isEqualTo(rating);
        assertThat(result.getReviewComment()).isEqualTo(comment);
        assertThat(result.getSessionStatus()).isEqualTo(SessionStatus.COMPLETED);
        verify(coachServiceClient).updateCoachRating(eq(1L), any(BigDecimal.class), eq(5L));
        verify(userService).incrementSessionsTaken(1L);
    }

    @Test
    void updateSessionRating_nonExistingSession_throwsException() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());
        BigDecimal rating = BigDecimal.valueOf(8.0);

        assertThatThrownBy(() -> sessionRatingService.updateSessionRating(99L, rating, null))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSessionRating_nullAverageRating_skipsCoachUpdate() {
        BigDecimal rating = BigDecimal.valueOf(7.0);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
        when(sessionRepository.getAverageRatingByCoachId(1L)).thenReturn(null);
        when(sessionRepository.countSessionsByCoachId(1L)).thenReturn(0L);
        when(userService.incrementSessionsTaken(1L)).thenReturn(testUser);

        sessionRatingService.updateSessionRating(1L, rating, null);

        verify(coachServiceClient, never()).updateCoachRating(any(), any(), any());
    }

    @Test
    void updateSessionRating_withNullComment_setsNullComment() {
        BigDecimal rating = BigDecimal.valueOf(9.0);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
        when(sessionRepository.getAverageRatingByCoachId(1L)).thenReturn(9.0);
        when(sessionRepository.countSessionsByCoachId(1L)).thenReturn(1L);
        when(userService.incrementSessionsTaken(1L)).thenReturn(testUser);

        Session result = sessionRatingService.updateSessionRating(1L, rating, null);

        assertThat(result.getReviewComment()).isNull();
    }
}
