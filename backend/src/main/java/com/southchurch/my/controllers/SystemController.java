package com.southchurch.my.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.southchurch.my.services.GoogleWorkspaceService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {

    // Public Health Check (Used by Cloud Run / Load Balancers)
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("service", "My South Church Backend");

        return ResponseEntity.ok(status);
    }

    // Simple Ping (For quick browser checks)
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    // Google Connectivity Test
    @Autowired
    private GoogleWorkspaceService googleService;

    @GetMapping("/google-test")
    public String testGoogle() {
        return googleService.runConnectivityTest();
    }
}