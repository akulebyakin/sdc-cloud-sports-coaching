package com.kulebiakin.sportscoaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.sportscoaching.entity.User;
import com.kulebiakin.sportscoaching.service.UserService;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .firstName("Alice")
                .lastName("Johnson")
                .sessionsTaken(5)
                .build();
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() throws Exception {
            User user2 = User.builder()
                    .userId(2L)
                    .firstName("Bob")
                    .lastName("Williams")
                    .sessionsTaken(3)
                    .build();

            when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, user2));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].firstName", is("Alice")))
                    .andExpect(jsonPath("$[1].firstName", is("Bob")));
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyList() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() throws Exception {
            when(userService.getUserById(1L)).thenReturn(testUser);

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("Alice")))
                    .andExpect(jsonPath("$.lastName", is("Johnson")));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(userService.getUserById(99L)).thenThrow(new EntityNotFoundException("User not found"));

            mockMvc.perform(get("/api/users/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUser() throws Exception {
            when(userService.createUser(any(User.class))).thenReturn(testUser);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUser)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("Alice")));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUser() throws Exception {
            User updatedUser = User.builder()
                    .userId(1L)
                    .firstName("UpdatedAlice")
                    .lastName("UpdatedJohnson")
                    .build();

            when(userService.updateUser(anyLong(), any(User.class))).thenReturn(updatedUser);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName", is("UpdatedAlice")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUser() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(1L);
        }
    }
}
