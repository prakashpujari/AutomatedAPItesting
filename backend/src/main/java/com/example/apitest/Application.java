package com.example.apitest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API Testing Platform backend.
 *
 * The application starts a Spring Boot web server that exposes REST endpoints for the
 * React UI and triggers the LangGraph orchestrator.
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
