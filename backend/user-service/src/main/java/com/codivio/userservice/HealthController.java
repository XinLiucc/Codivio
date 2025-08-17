package com.codivio.userservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/v1/users/health")
    public String health() {
        return "User Service is running!";
    }
}