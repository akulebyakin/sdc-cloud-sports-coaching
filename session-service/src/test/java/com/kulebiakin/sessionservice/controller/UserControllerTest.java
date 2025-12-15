package com.kulebiakin.sessionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.exception.ResourceNotFoundException;
import com.kulebiakin.sessionservice.entity.User;
import com.kulebiakin.sessionservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
            .firstName("John")
            .lastName("Doe")
            .sessionsTaken(0)
            .build();
    }

    @Test
    void getAllUsers_returnsOkWithList() throws Exception {
        List<User> users = Collections.singletonList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void getUserById_existingId_returnsOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getUserById_nonExistingId_returnsNotFound() throws Exception {
        when(userService.getUserById(99L))
            .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/api/users/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createUser_validData_returnsCreated() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void createUser_missingFirstName_returnsBadRequest() throws Exception {
        User invalidUser = User.builder()
            .lastName("Doe")
            .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_existingId_returnsOk() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
            .andExpect(status().isOk());
    }

    @Test
    void updateUser_nonExistingId_returnsNotFound() throws Exception {
        when(userService.updateUser(eq(99L), any(User.class)))
            .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(put("/api/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_existingId_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_nonExistingId_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User", 99L))
            .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
            .andExpect(status().isNotFound());
    }
}
