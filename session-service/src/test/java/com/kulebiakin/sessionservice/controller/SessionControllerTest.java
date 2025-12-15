package com.kulebiakin.sessionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.entity.Session;
import com.kulebiakin.sessionservice.entity.SessionStatus;
import com.kulebiakin.sessionservice.entity.User;
import com.kulebiakin.sessionservice.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessionService sessionService;

    private Session testSession;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
            .userId(1L)
            .firstName("John")
            .lastName("Doe")
            .sessionsTaken(0)
            .build();

        testSession = Session.builder()
            .sessionId(1L)
            .sessionDateTime(LocalDateTime.of(2024, 12, 25, 10, 0))
            .sessionStatus(SessionStatus.SCHEDULED)
            .coachId(1L)
            .user(testUser)
            .build();
    }

    @Test
    void getAllSessions_returnsOkWithList() throws Exception {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionService.getAllSessions()).thenReturn(sessions);

        mockMvc.perform(get("/api/sessions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].sessionStatus").value("SCHEDULED"));
    }

    @Test
    void getSessionById_existingId_returnsOk() throws Exception {
        when(sessionService.getSessionById(1L)).thenReturn(testSession);

        mockMvc.perform(get("/api/sessions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionStatus").value("SCHEDULED"));
    }

    @Test
    void getSessionById_nonExistingId_returnsNotFound() throws Exception {
        when(sessionService.getSessionById(99L))
            .thenThrow(new ResourceNotFoundException("Session", 99L));

        mockMvc.perform(get("/api/sessions/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getSessionsByCoachId_returnsOk() throws Exception {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionService.getSessionsByCoachId(1L)).thenReturn(sessions);

        mockMvc.perform(get("/api/sessions/coach/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].coachId").value(1));
    }

    @Test
    void getSessionsByUserId_returnsOk() throws Exception {
        List<Session> sessions = Collections.singletonList(testSession);
        when(sessionService.getSessionsByUserId(1L)).thenReturn(sessions);

        mockMvc.perform(get("/api/sessions/user/1"))
            .andExpect(status().isOk());
    }

    @Test
    void createSession_validData_returnsCreated() throws Exception {
        when(sessionService.createSession(eq(1L), eq(1L), any(Session.class))).thenReturn(testSession);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1);
        payload.put("coachId", 1);
        payload.put("sessionDateTime", "2024-12-25T10:00:00");

        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());
    }

    @Test
    void updateSession_existingId_returnsOk() throws Exception {
        when(sessionService.updateSession(eq(1L), any(Session.class))).thenReturn(testSession);

        mockMvc.perform(put("/api/sessions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSession)))
            .andExpect(status().isOk());
    }

    @Test
    void updateSession_nonExistingId_returnsNotFound() throws Exception {
        when(sessionService.updateSession(eq(99L), any(Session.class)))
            .thenThrow(new ResourceNotFoundException("Session", 99L));

        mockMvc.perform(put("/api/sessions/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSession)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateSessionRating_validRequest_returnsOk() throws Exception {
        testSession.setRating(BigDecimal.valueOf(9.0));
        when(sessionService.updateSessionRating(eq(1L), any(BigDecimal.class), anyString()))
            .thenReturn(testSession);

        Map<String, Object> payload = new HashMap<>();
        payload.put("rating", 9.0);
        payload.put("comment", "Great session!");

        mockMvc.perform(post("/api/sessions/1/rating")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk());
    }

    @Test
    void deleteSession_existingId_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/sessions/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteSession_nonExistingId_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Session", 99L))
            .when(sessionService).deleteSession(99L);

        mockMvc.perform(delete("/api/sessions/99"))
            .andExpect(status().isNotFound());
    }
}
