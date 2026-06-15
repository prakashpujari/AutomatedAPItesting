package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Analyzes the overall run (flaky tests, slow APIs, coverage gaps) using
 * the DeepSeek R1 model and returns a textual optimization suggestion.
 */
@Component
public class OptimizationAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public OptimizationAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        // Gather a few pieces of data that the model can reason about.
        String execResult = (String) context.getOrDefault("executionResult", "");
        String validation = (String) context.getOrDefault("validationResult", "");
        String failure = (String) context.getOrDefault("failureReport", "");
        String prompt = """
            You are a performance-and-quality engineer. Analyze the execution result, validation outcome, and failure analysis (if any) and produce a concise optimization suggestion that includes:

            - Additional test cases that would improve coverage (specify endpoint and category).
            - Refactoring ideas for the generated test code (e.g., reusable request builders, parameter factories).
            - Performance improvements (e.g., parallel execution, timeout tuning, caching of static data).
            - Any flaky-test mitigation strategies.

            Return the suggestion as a single plain-text sentence without markup or JSON.
            """;
        String suggestion = llmService.complete("deepseek-r1-distill-llama-70b", prompt + "\n\nExecution Result:\n" + execResult + "\n\nValidation:\n" + validation + "\n\nFailure Report:\n" + failure);
        context.put("optimizationSuggestion", suggestion);
        return suggestion;
    }
}