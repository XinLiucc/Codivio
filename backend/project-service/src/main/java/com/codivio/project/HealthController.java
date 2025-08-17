package com.codivio.project;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/projects/health")
    public String health() {
        return "Project Service is running!";
    }
}