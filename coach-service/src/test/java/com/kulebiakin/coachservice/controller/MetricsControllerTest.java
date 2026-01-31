package com.kulebiakin.coachservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMetrics_returnsOkStatus() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getMetrics_containsServiceName() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("coach-service"));
    }

    @Test
    void getMetrics_containsTimestamp() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getMetrics_containsJvmMetrics() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jvm").exists())
            .andExpect(jsonPath("$.jvm.uptime_seconds").exists())
            .andExpect(jsonPath("$.jvm.heap_used_mb").exists())
            .andExpect(jsonPath("$.jvm.heap_max_mb").exists())
            .andExpect(jsonPath("$.jvm.non_heap_used_mb").exists())
            .andExpect(jsonPath("$.jvm.available_processors").exists());
    }

    @Test
    void getMetrics_containsThreadMetrics() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads").exists())
            .andExpect(jsonPath("$.threads.thread_count").exists())
            .andExpect(jsonPath("$.threads.peak_thread_count").exists());
    }

    @Test
    void getMetrics_jvmMetricsAreNonNegative() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jvm.uptime_seconds").isNumber())
            .andExpect(jsonPath("$.jvm.heap_used_mb").isNumber())
            .andExpect(jsonPath("$.jvm.available_processors").isNumber());
    }

    @Test
    void getMetrics_threadCountIsPositive() throws Exception {
        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.threads.thread_count").isNumber());
    }
}
