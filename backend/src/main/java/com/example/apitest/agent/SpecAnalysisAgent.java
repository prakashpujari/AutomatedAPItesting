package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Analyzes the API catalog (produced by ApiDiscoveryAgent) and generates a
 * testing strategy / coverage matrix using the Groq model.
 */
@Component
public class SpecAnalysisAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public SpecAnalysisAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String catalog = (String) context.getOrDefault("apiCatalog", "{}");
        String prompt = """
            You are an expert test-strategy architect. Using the API catalog provided (a JSON object containing endpoints, methods, parameters, authentication, and schemas), create a testing strategy that includes:

            - Test categories (functional, security, performance, contract, negative, boundary, authentication, rate-limit).
            - Key edge cases for each endpoint (e.g., missing required fields, oversized payloads, malformed JSON, unauthorized access).
            - Parameter validation rules (min/max, regex, enum values, nullability).
            - Coverage matrix mapping each endpoint to the required test categories.

            Return the strategy as a compact JSON object without markdown fences or any explanatory text.
            """;
        String completion = llmService.complete("llama-3.1-8b-instant", prompt + "\nCatalog:\n" + catalog);
        // Strip markdown code fences if present
        completion = completion.trim();
        int startIdx = completion.indexOf('{');
        int endIdx = completion.lastIndexOf('}') + 1;
        if (startIdx >= 0 && endIdx > startIdx) {
            completion = completion.substring(startIdx, endIdx);
        }
        context.put("testingStrategy", completion);
        return completion;
    }
}