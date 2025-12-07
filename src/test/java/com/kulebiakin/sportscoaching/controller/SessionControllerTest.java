package com.kulebiakin.sportscoaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.sportscoaching.entity.*;
import com.kulebiakin.sportscoaching.service.SessionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
                .sessionDateTime(LocalDateTime.of(2024, 6, 15, 10, 0))
                .sessionStatus(SessionStatus.SCHEDULED)
                .coach(testCoach)
                .user(testUser)
                .build();
    }

    @Nested
    @DisplayName("GET /api/sessions")
    class GetAllSessions {

        @Test
        @DisplayName("should return all sessions")
        void shouldReturnAllSessions() throws Exception {
            Session session2 = Session.builder()
                    .sessionId(2L)
                    .sessionDateTime(LocalDateTime.of(2024, 6, 16, 14, 0))
                    .sessionStatus(SessionStatus.COMPLETED)
                    .coach(testCoach)
                    .user(testUser)
                    .build();

            when(sessionService.getAllSessions()).thenReturn(Arrays.asList(testSession, session2));

            mockMvc.perform(get("/api/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].sessionStatus", is("SCHEDULED")))
                    .andExpect(jsonPath("$[1].sessionStatus", is("COMPLETED")));
        }

        @Test
        @DisplayName("should return empty list when no sessions")
        void shouldReturnEmptyList() throws Exception {
            when(sessionService.getAllSessions()).thenReturn(List.of());

            mockMvc.perform(get("/api/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/sessions/{id}")
    class GetSessionById {

        @Test
        @DisplayName("should return session when found")
        void shouldReturnSessionWhenFound() throws Exception {
            when(sessionService.getSessionById(1L)).thenReturn(testSession);

            mockMvc.perform(get("/api/sessions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId", is(1)))
                    .andExpect(jsonPath("$.sessionStatus", is("SCHEDULED")));
        }

        @Test
        @DisplayName("should return 404 when session not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(sessionService.getSessionById(99L)).thenThrow(new EntityNotFoundException("Session not found"));

            mockMvc.perform(get("/api/sessions/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/sessions/coach/{coachId}")
    class GetSessionsByCoachId {

        @Test
        @DisplayName("should return sessions for coach")
        void shouldReturnSessionsForCoach() throws Exception {
            when(sessionService.getSessionsByCoachId(1L)).thenReturn(List.of(testSession));

            mockMvc.perform(get("/api/sessions/coach/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /api/sessions/user/{userId}")
    class GetSessionsByUserId {

        @Test
        @DisplayName("should return sessions for user")
        void shouldReturnSessionsForUser() throws Exception {
            when(sessionService.getSessionsByUserId(1L)).thenReturn(List.of(testSession));

            mockMvc.perform(get("/api/sessions/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("POST /api/sessions")
    class CreateSession {

        @Test
        @DisplayName("should create session successfully")
        void shouldCreateSession() throws Exception {
            when(sessionService.createSession(anyLong(), anyLong(), any(Session.class))).thenReturn(testSession);

            Map<String, Object> payload = Map.of(
                    "userId", 1,
                    "coachId", 1,
                    "sessionDateTime", "2024-06-15T10:00:00"
            );

            mockMvc.perform(post("/api/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sessionId", is(1)));
        }
    }

    @Nested
    @DisplayName("POST /api/sessions/{id}/rating")
    class UpdateSessionRating {

        @Test
        @DisplayName("should update session rating")
        void shouldUpdateSessionRating() throws Exception {
            testSession.setRating(BigDecimal.valueOf(9.0));
            testSession.setSessionStatus(SessionStatus.COMPLETED);
            when(sessionService.updateSessionRating(anyLong(), any(BigDecimal.class))).thenReturn(testSession);

            Map<String, Object> payload = Map.of("rating", 9.0);

            mockMvc.perform(post("/api/sessions/1/rating")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating", is(9.0)))
                    .andExpect(jsonPath("$.sessionStatus", is("COMPLETED")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/sessions/{id}")
    class DeleteSession {

        @Test
        @DisplayName("should delete session successfully")
        void shouldDeleteSession() throws Exception {
            doNothing().when(sessionService).deleteSession(1L);

            mockMvc.perform(delete("/api/sessions/1"))
                    .andExpect(status().isNoContent());

            verify(sessionService).deleteSession(1L);
        }
    }
}
