package com.kulebiakin.reviewservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom metrics endpoint providing basic application metrics.
 * For full metrics use /actuator/prometheus or /actuator/metrics
 */
@RestController
@Tag(name = "Metrics", description = "Custom application metrics endpoint")
public class MetricsController {

    @GetMapping("/metrics")
    @Operation(summary = "Get custom application metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        // Application info
        metrics.put("service", "review-service");
        metrics.put("timestamp", Instant.now().toString());

        // JVM metrics
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        Map<String, Object> jvmMetrics = new LinkedHashMap<>();
        jvmMetrics.put("uptime_seconds", runtimeMXBean.getUptime() / 1000);
        jvmMetrics.put("heap_used_mb", memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        jvmMetrics.put("heap_max_mb", memoryMXBean.getHeapMemoryUsage().getMax() / (1024 * 1024));
        jvmMetrics.put("non_heap_used_mb", memoryMXBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024));
        jvmMetrics.put("available_processors", Runtime.getRuntime().availableProcessors());
        metrics.put("jvm", jvmMetrics);

        // Thread metrics
        Map<String, Object> threadMetrics = new LinkedHashMap<>();
        threadMetrics.put("thread_count", ManagementFactory.getThreadMXBean().getThreadCount());
        threadMetrics.put("peak_thread_count", ManagementFactory.getThreadMXBean().getPeakThreadCount());
        metrics.put("threads", threadMetrics);

        return ResponseEntity.ok(metrics);
    }
}
