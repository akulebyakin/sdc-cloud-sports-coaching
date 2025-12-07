package com.kulebiakin.sportscoaching.service;

import com.kulebiakin.sportscoaching.entity.*;
import com.kulebiakin.sportscoaching.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserService userService;

    @Mock
    private CoachService coachService;

    @InjectMocks
    private SessionService sessionService;

    private Session testSession;
    private User testUser;
    private Coach testCoach;

    @BeforeEach
    void setUp() {
        testCoach = Coach.builder()
                .coachId(1L)
                .firstName("John")
                .lastName("Doe")
                .rating(BigDecimal.valueOf(8.5))
                .strikeCount(0)
                .coachStatus(CoachStatus.ACTIVE)
                .build();

        testUser = User.builder()
                .userId(1L)
                .firstName("Alice")
                .lastName("Johnson")
                .sessionsTaken(5)
                .build();

        testSession = Session.builder()
                .sessionId(1L)
                .sessionDateTime(LocalDateTime.now().plusDays(1))
                .sessionStatus(SessionStatus.SCHEDULED)
                .coach(testCoach)
                .user(testUser)
                .build();
    }

    @Nested
    @DisplayName("getAllSessions")
    class GetAllSessions {

        @Test
        @DisplayName("should return all sessions")
        void shouldReturnAllSessions() {
            Session session2 = Session.builder()
                    .sessionId(2L)
                    .sessionDateTime(LocalDateTime.now().plusDays(2))
                    .sessionStatus(SessionStatus.SCHEDULED)
                    .coach(testCoach)
                    .user(testUser)
                    .build();

            when(sessionRepository.findAll()).thenReturn(Arrays.asList(testSession, session2));

            List<Session> result = sessionService.getAllSessions();

            assertThat(result).hasSize(2);
            verify(sessionRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no sessions exist")
        void shouldReturnEmptyListWhenNoSessions() {
            when(sessionRepository.findAll()).thenReturn(List.of());

            List<Session> result = sessionService.getAllSessions();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSessionById")
    class GetSessionById {

        @Test
        @DisplayName("should return session when found")
        void shouldReturnSessionWhenFound() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

            Session result = sessionService.getSessionById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getSessionStatus()).isEqualTo(SessionStatus.SCHEDULED);
        }

        @Test
        @DisplayName("should throw exception when session not found")
        void shouldThrowExceptionWhenNotFound() {
            when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.getSessionById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Session not found with id: 99");
        }
    }

    @Nested
    @DisplayName("getSessionsByCoachId")
    class GetSessionsByCoachId {

        @Test
        @DisplayName("should return sessions for a coach")
        void shouldReturnSessionsForCoach() {
            when(sessionRepository.findByCoachCoachId(1L)).thenReturn(List.of(testSession));

            List<Session> result = sessionService.getSessionsByCoachId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCoach().getCoachId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getSessionsByUserId")
    class GetSessionsByUserId {

        @Test
        @DisplayName("should return sessions for a user")
        void shouldReturnSessionsForUser() {
            when(sessionRepository.findByUserUserId(1L)).thenReturn(List.of(testSession));

            List<Session> result = sessionService.getSessionsByUserId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("createSession")
    class CreateSession {

        @Test
        @DisplayName("should create session successfully")
        void shouldCreateSession() {
            Session newSession = Session.builder()
                    .sessionDateTime(LocalDateTime.now().plusDays(3))
                    .build();

            when(userService.getUserById(1L)).thenReturn(testUser);
            when(coachService.getCoachById(1L)).thenReturn(testCoach);
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

            Session result = sessionService.createSession(1L, 1L, newSession);

            assertThat(result).isNotNull();
            verify(userService).getUserById(1L);
            verify(coachService).getCoachById(1L);
            verify(sessionRepository).save(newSession);
        }
    }

    @Nested
    @DisplayName("updateSession")
    class UpdateSession {

        @Test
        @DisplayName("should update session successfully")
        void shouldUpdateSession() {
            Session updatedDetails = Session.builder()
                    .sessionDateTime(LocalDateTime.now().plusDays(5))
                    .sessionStatus(SessionStatus.COMPLETED)
                    .build();

            when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

            Session result = sessionService.updateSession(1L, updatedDetails);

            assertThat(result.getSessionStatus()).isEqualTo(SessionStatus.COMPLETED);
            verify(sessionRepository).save(testSession);
        }
    }

    @Nested
    @DisplayName("updateSessionRating")
    class UpdateSessionRating {

        @Test
        @DisplayName("should update session rating and coach rating")
        void shouldUpdateSessionRating() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
            when(sessionRepository.getAverageRatingByCoachId(1L)).thenReturn(8.0);
            when(sessionRepository.countSessionsByCoachId(1L)).thenReturn(10L);
            when(coachService.updateCoachRating(anyLong(), any(BigDecimal.class), anyLong())).thenReturn(testCoach);
            when(userService.incrementSessionsTaken(1L)).thenReturn(testUser);

            Session result = sessionService.updateSessionRating(1L, BigDecimal.valueOf(9.0));

            assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(9.0));
            assertThat(result.getSessionStatus()).isEqualTo(SessionStatus.COMPLETED);
            verify(coachService).updateCoachRating(1L, BigDecimal.valueOf(8.0), 10L);
            verify(userService).incrementSessionsTaken(1L);
        }

        @Test
        @DisplayName("should handle null average rating")
        void shouldHandleNullAverageRating() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
            when(sessionRepository.save(any(Session.class))).thenReturn(testSession);
            when(sessionRepository.getAverageRatingByCoachId(1L)).thenReturn(null);
            when(sessionRepository.countSessionsByCoachId(1L)).thenReturn(0L);
            when(userService.incrementSessionsTaken(1L)).thenReturn(testUser);

            Session result = sessionService.updateSessionRating(1L, BigDecimal.valueOf(7.0));

            assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(7.0));
            verify(coachService, never()).updateCoachRating(anyLong(), any(), anyLong());
        }
    }

    @Nested
    @DisplayName("deleteSession")
    class DeleteSession {

        @Test
        @DisplayName("should delete session successfully")
        void shouldDeleteSession() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
            doNothing().when(sessionRepository).delete(testSession);

            sessionService.deleteSession(1L);

            verify(sessionRepository).delete(testSession);
        }

        @Test
        @DisplayName("should throw exception when deleting non-existent session")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sessionService.deleteSession(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
