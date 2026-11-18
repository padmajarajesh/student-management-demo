package com.demo.student.controller;

import com.demo.student.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Public health check. Used by Render's health check and handy for verifying
 * that the backend can reach the database after deployment.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        boolean dbUp;
        try {
            dbUp = Integer.valueOf(1).equals(jdbcTemplate.queryForObject("SELECT 1", Integer.class));
        } catch (Exception e) {
            dbUp = false;
        }
        Map<String, String> status = Map.of(
                "status", dbUp ? "UP" : "DOWN",
                "database", dbUp ? "UP" : "DOWN");
        return ResponseEntity.status(dbUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiResponse<>(dbUp, dbUp ? "Service is healthy" : "Database unavailable",
                        status, null, Instant.now()));
    }
}
