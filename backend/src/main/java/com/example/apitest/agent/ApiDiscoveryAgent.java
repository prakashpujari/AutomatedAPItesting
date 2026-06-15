package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Discovers APIs from supplied sources (OpenAPI URLs, Postman collections, etc.).
 * Uses the external LLM gateway (model: llama-3.1-8b-instant) to transform raw source descriptors
 * into a structured API catalog.
 */
@Component
public class ApiDiscoveryAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public ApiDiscoveryAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        Object sources = context.getOrDefault("apiSources", "[]");
        String prompt = """
            You are an API discovery specialist. Given a list of API source descriptors (OpenAPI specs, RAML files, Postman collections, Maven repo URLs, GitHub repos, Azure DevOps artifacts, etc.), extract a complete, machine-readable JSON catalog that includes:

            - Every endpoint path
            - HTTP method
            - Required and optional headers
            - Path, query, and body parameters (with types, constraints, defaults)
            - Authentication mechanisms (API keys, OAuth2 flows, JWT, etc.)
            - Request and response schema references (JSON Schema, Avro, Protobuf)
            - Any discovered rate-limit or pagination details

            Provide the catalog as a single JSON object without any surrounding text or markdown.
            """;
        String completion = llmService.complete("llama-3.1-8b-instant", prompt + "Sources:\n" + sources);
        // Extract JSON content
        completion = completion.trim();
        int startIdx = completion.indexOf('{');
        int endIdx = completion.lastIndexOf('}') + 1;
        if (startIdx >= 0 && endIdx > startIdx) {
            completion = completion.substring(startIdx, endIdx);
        }
        // Store raw catalog string; later agents can parse as needed.
        context.put("apiCatalog", completion);
        return completion;
    }
}