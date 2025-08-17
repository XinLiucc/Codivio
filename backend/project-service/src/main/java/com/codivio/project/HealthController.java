package com.codivio.project;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/project/health")
    public String health() {
        return "Project Service is running!";
    }
}