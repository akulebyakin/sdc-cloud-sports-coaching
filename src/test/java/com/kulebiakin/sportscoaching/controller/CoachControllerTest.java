package com.kulebiakin.sportscoaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.sportscoaching.entity.Coach;
import com.kulebiakin.sportscoaching.entity.CoachStatus;
import com.kulebiakin.sportscoaching.service.CoachService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .rating(BigDecimal.valueOf(8.5))
                .strikeCount(0)
                .coachStatus(CoachStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("GET /api/coaches")
    class GetAllCoaches {

        @Test
        @DisplayName("should return all coaches")
        void shouldReturnAllCoaches() throws Exception {
            Coach coach2 = Coach.builder()
                    .coachId(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            when(coachService.getAllCoaches()).thenReturn(Arrays.asList(testCoach, coach2));

            mockMvc.perform(get("/api/coaches"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].firstName", is("John")))
                    .andExpect(jsonPath("$[1].firstName", is("Jane")));
        }

        @Test
        @DisplayName("should return empty list when no coaches")
        void shouldReturnEmptyList() throws Exception {
            when(coachService.getAllCoaches()).thenReturn(List.of());

            mockMvc.perform(get("/api/coaches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/coaches/{id}")
    class GetCoachById {

        @Test
        @DisplayName("should return coach when found")
        void shouldReturnCoachWhenFound() throws Exception {
            when(coachService.getCoachById(1L)).thenReturn(testCoach);

            mockMvc.perform(get("/api/coaches/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.coachId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")))
                    .andExpect(jsonPath("$.lastName", is("Doe")));
        }

        @Test
        @DisplayName("should return 404 when coach not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(coachService.getCoachById(99L)).thenThrow(new EntityNotFoundException("Coach not found"));

            mockMvc.perform(get("/api/coaches/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/coaches")
    class CreateCoach {

        @Test
        @DisplayName("should create coach successfully")
        void shouldCreateCoach() throws Exception {
            when(coachService.createCoach(any(Coach.class))).thenReturn(testCoach);

            mockMvc.perform(post("/api/coaches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCoach)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.coachId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")));
        }
    }

    @Nested
    @DisplayName("PUT /api/coaches/{id}")
    class UpdateCoach {

        @Test
        @DisplayName("should update coach successfully")
        void shouldUpdateCoach() throws Exception {
            Coach updatedCoach = Coach.builder()
                    .coachId(1L)
                    .firstName("UpdatedJohn")
                    .lastName("UpdatedDoe")
                    .build();

            when(coachService.updateCoach(anyLong(), any(Coach.class))).thenReturn(updatedCoach);

            mockMvc.perform(put("/api/coaches/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedCoach)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName", is("UpdatedJohn")));
        }
    }

    @Nested
    @DisplayName("POST /api/coaches/rating")
    class UpdateCoachRating {

        @Test
        @DisplayName("should update coach rating")
        void shouldUpdateCoachRating() throws Exception {
            testCoach.setRating(BigDecimal.valueOf(9.0));
            when(coachService.updateCoachRating(anyLong(), any(BigDecimal.class), anyLong())).thenReturn(testCoach);

            Map<String, Object> payload = Map.of(
                    "coachId", 1,
                    "rating", 9.0,
                    "totalSessions", 10
            );

            mockMvc.perform(post("/api/coaches/rating")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating", is(9.0)));
        }
    }

    @Nested
    @DisplayName("POST /api/coaches/status")
    class UpdateCoachStatus {

        @Test
        @DisplayName("should update coach status")
        void shouldUpdateCoachStatus() throws Exception {
            testCoach.setCoachStatus(CoachStatus.DEACTIVATED);
            when(coachService.updateCoachStatus(anyLong(), any(CoachStatus.class))).thenReturn(testCoach);

            Map<String, Object> payload = Map.of(
                    "coachId", 1,
                    "status", "DEACTIVATED"
            );

            mockMvc.perform(post("/api/coaches/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.coachStatus", is("DEACTIVATED")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/coaches/{id}")
    class DeleteCoach {

        @Test
        @DisplayName("should delete coach successfully")
        void shouldDeleteCoach() throws Exception {
            doNothing().when(coachService).deleteCoach(1L);

            mockMvc.perform(delete("/api/coaches/1"))
                    .andExpect(status().isNoContent());

            verify(coachService).deleteCoach(1L);
        }
    }
}
