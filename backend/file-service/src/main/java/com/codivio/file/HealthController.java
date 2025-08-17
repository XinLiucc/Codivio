package com.codivio.file;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/file/health")
    public String health() {
        return "File Service is running!";
    }
}