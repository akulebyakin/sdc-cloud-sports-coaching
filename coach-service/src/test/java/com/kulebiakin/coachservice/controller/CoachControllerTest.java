package com.kulebiakin.coachservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.coachservice.entity.Coach;
import com.kulebiakin.coachservice.entity.CoachStatus;
import com.kulebiakin.coachservice.service.CoachService;
import com.kulebiakin.common.dto.CoachRatingRequest;
import com.kulebiakin.common.dto.CoachStatusRequest;
import com.kulebiakin.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(CoachController.class)
class CoachControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoachService coachService;

    private Coach testCoach;

    @BeforeEach
    void setUp() {
        testCoach = Coach.builder()
            .coachId(1L)
            .firstName("John")
            .lastName("Doe")
            .rating(BigDecimal.valueOf(7.5))
            .strikeCount(0)
            .coachStatus(CoachStatus.ACTIVE)
            .build();
    }

    @Test
    void getAllCoaches_returnsOkWithList() throws Exception {
        List<Coach> coaches = Collections.singletonList(testCoach);
        when(coachService.getAllCoaches()).thenReturn(coaches);

        mockMvc.perform(get("/api/coaches"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void getCoachById_existingId_returnsOk() throws Exception {
        when(coachService.getCoachById(1L)).thenReturn(testCoach);

        mockMvc.perform(get("/api/coaches/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getCoachById_nonExistingId_returnsNotFound() throws Exception {
        when(coachService.getCoachById(99L))
            .thenThrow(new ResourceNotFoundException("Coach", 99L));

        mockMvc.perform(get("/api/coaches/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createCoach_validData_returnsCreated() throws Exception {
        when(coachService.createCoach(any(Coach.class))).thenReturn(testCoach);

        mockMvc.perform(post("/api/coaches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCoach)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void createCoach_missingFirstName_returnsBadRequest() throws Exception {
        Coach invalidCoach = Coach.builder()
            .lastName("Doe")
            .build();

        mockMvc.perform(post("/api/coaches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCoach)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateCoach_existingId_returnsOk() throws Exception {
        when(coachService.updateCoach(eq(1L), any(Coach.class))).thenReturn(testCoach);

        mockMvc.perform(put("/api/coaches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCoach)))
            .andExpect(status().isOk());
    }

    @Test
    void updateCoach_nonExistingId_returnsNotFound() throws Exception {
        when(coachService.updateCoach(eq(99L), any(Coach.class)))
            .thenThrow(new ResourceNotFoundException("Coach", 99L));

        mockMvc.perform(put("/api/coaches/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCoach)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateCoachRating_validRequest_returnsOk() throws Exception {
        CoachRatingRequest request = CoachRatingRequest.builder()
            .coachId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .totalSessions(10L)
            .build();

        when(coachService.updateCoachRating(1L, BigDecimal.valueOf(8.5), 10L))
            .thenReturn(testCoach);

        mockMvc.perform(post("/api/coaches/rating")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void updateCoachStatus_validRequest_returnsOk() throws Exception {
        CoachStatusRequest request = CoachStatusRequest.builder()
            .coachId(1L)
            .status("DEACTIVATED")
            .build();

        testCoach.setCoachStatus(CoachStatus.DEACTIVATED);
        when(coachService.updateCoachStatus(1L, CoachStatus.DEACTIVATED))
            .thenReturn(testCoach);

        mockMvc.perform(post("/api/coaches/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.coachStatus").value("DEACTIVATED"));
    }

    @Test
    void deleteCoach_existingId_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/coaches/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteCoach_nonExistingId_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Coach", 99L))
            .when(coachService).deleteCoach(99L);

        mockMvc.perform(delete("/api/coaches/99"))
            .andExpect(status().isNotFound());
    }
}
