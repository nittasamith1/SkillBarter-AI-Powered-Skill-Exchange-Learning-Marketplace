package com.skillbarter.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint verifying system operational status and DB connectivity.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application health status endpoint")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @Operation(summary = "Health check", description = "Returns system status and verifies database connectivity")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean dbStatus = checkDbConnection();

        if (!dbStatus) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "database", "DOWN"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "database", "UP",
                "phase", "1 — Foundation + Identity + Multi-Tenancy"
        ));
    }

    private boolean checkDbConnection() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (Exception e) {
            return false;
        }
    }
}
