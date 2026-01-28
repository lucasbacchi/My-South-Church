package com.southchurch.my;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
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
}