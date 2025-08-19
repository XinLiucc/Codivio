package com.codivio.file;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/files/health")
    public String health() {
        return "File Service is running!";
    }
}