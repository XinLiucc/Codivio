package com.codivio.collaboration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/collaboration/health")
    public String health() {
        return "Collaboration Service is running!";
    }
}