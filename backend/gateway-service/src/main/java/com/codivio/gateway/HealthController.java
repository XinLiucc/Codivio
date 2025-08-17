package com.codivio.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/gateway/health")
    public String health() {
        return "Gateway Service is running!";
    }
}