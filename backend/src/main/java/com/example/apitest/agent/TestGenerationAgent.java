package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Generates production-grade Java test code (Rest-Assured + JUnit5) using the
 * Groq model. The generated source is placed in the shared context under the
 * key {@code generatedTestCode} for the ExecutionAgent to write to disk.
 */
@Component
public class TestGenerationAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public TestGenerationAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String strategy = (String) context.getOrDefault("testingStrategy", "");
        if (strategy == null || strategy.isBlank()) {
            strategy = "{}";
        }
        String prompt = """
            You are a senior Java test engineer specialized in RestAssured and JUnit5. Using the testing strategy JSON provided (which maps each API endpoint to required test categories and edge cases), generate production-grade Java test code that satisfies the following constraints:

            - Use package com.example.apitest.generated.
            - Include all necessary imports (io.restassured.RestAssured, org.junit.jupiter.api.*, any JSON handling utilities).
            - Define a public class named SampleApiTest.
            - For each endpoint in the strategy, create separate test methods that cover:
              * Positive happy-path scenarios (valid request, expected 2xx response).
              * Negative cases (missing required fields, invalid enums, malformed JSON) that exercise validation errors.
              * Boundary cases (min/max values, empty strings, oversized payloads).
              * Security tests (unauthenticated, wrong token, role-based access failures).
              * Performance placeholders (use @Timeout or simple timing assertions).
              * Contract assertions that verify response schema compliance (you may call a placeholder validator method).
            - Parameter values should be drawn from the testDataSet context entry when available; otherwise use sensible literals.
            - Do not include any explanatory text, markdown fences, or comments beyond necessary Java comments.
            - Return only the raw Java source code as a single string.
            """;
        String completion = llmService.complete("llama-3.1-8b-instant", prompt + "\nStrategy JSON:\n" + strategy);
        // Strip markdown code fences if present (for Java code)
        if (completion != null) {
            completion = completion.trim();
            // Find Java code start (package statement)
            int startIdx = completion.indexOf("package ");
            if (startIdx < 0) {
                startIdx = completion.indexOf("import ");
            }
            if (startIdx >= 0) {
                completion = completion.substring(startIdx);
            } else {
                // If no Java markers found, try to extract from markdown
                int mdStart = completion.indexOf("```java");
                if (mdStart >= 0) {
                    completion = completion.substring(mdStart + 7).trim();
                    int mdEnd = completion.indexOf("```");
                    if (mdEnd > 0) {
                        completion = completion.substring(0, mdEnd);
                    }
                }
            }
        }
        context.put("generatedTestCode", completion != null ? completion : "");
        return completion != null ? completion : "";
    }
}