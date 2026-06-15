package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Generates synthetic data sets (valid, invalid, boundary, masked-PII, etc.)
 * using the Groq model.
 */
@Component
public class TestDataAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public TestDataAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String prompt = """
            You are a test-data engineer. Generate a JSON object that provides example payloads for API testing. Include the following top-level keys, each mapping to an array of JSON objects that represent request bodies:

            - valid: well-formed requests that satisfy all schema constraints.
            - invalid: requests that deliberately violate required fields, type constraints, or formats.
            - boundary: requests that hit min/max limits, empty strings, zero-length arrays, etc.
            - maskedPII: requests containing personally identifiable information where the sensitive fields are masked (e.g., replace digits with asterisk).

            For each entry, include realistic field names and values (e.g., name, age, email, ssn). Return only the raw JSON string without markdown fences or extra text.
            """;
        String completion = llmService.complete("llama-3.1-8b-instant", prompt);
        // Extract JSON content
        if (completion != null) {
            completion = completion.trim();
            // Find JSON start
            int startIdx = completion.indexOf('{');
            int endIdx = completion.lastIndexOf('}') + 1;
            if (startIdx >= 0 && endIdx > startIdx) {
                completion = completion.substring(startIdx, endIdx);
            }
        }
        context.put("testDataSet", completion != null ? completion : "");
        return completion != null ? completion : "";
    }
}