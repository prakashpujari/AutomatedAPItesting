package com.example.apitest.web;

import com.example.apitest.langgraph.Orchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller that exposes the LangGraph orchestrator endpoint.
 */
@RestController
@RequestMapping("/api")
public class Controller {

    private final Orchestrator orchestrator;

    public Controller(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Run the full API testing pipeline.
     */
    @PostMapping("/orchestrate")
    public ResponseEntity<Map<String, Object>> orchestrate(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = orchestrator.run(input);
        return ResponseEntity.ok(result);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
}