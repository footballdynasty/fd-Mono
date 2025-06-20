package com.footballdynasty.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "football-dynasty-api");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "0.0.1-SNAPSHOT");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Football Dynasty API is running");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("docs", "/swagger-ui.html");
        
        return ResponseEntity.ok(response);
    }
}