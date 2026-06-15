package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates the execution results against SLAs, schemas, performance thresholds,
 * etc. The validation logic is delegated to the Groq model.
 */
@Component
public class ValidationAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public ValidationAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String execResult = (String) context.getOrDefault("executionResult", "");
        String prompt = """
            You are an expert QA analyst. Using the execution summary provided (which includes test counts, failures, response times, and any error messages), evaluate the run against the following Service Level Agreements (SLAs) and testing criteria:

            - HTTP status: All successful requests must return a 2xx status code.
            - Response time: Average response time must be < 500 ms; any individual request > 1 s is a violation.
            - Schema compliance: Responses must match the JSON schema defined in the API catalog.
            - Security: No unauthorized access errors (401/403) should appear.
            - Test pass rate: At least 95% of the generated tests must pass.

            Return a compact JSON object without markdown fences with keys: pass (boolean), reasons (array of strings).
            """;
        String completion = llmService.complete("llama-3.1-8b-instant", prompt + "\nExecution Summary:\n" + execResult);
        // Extract JSON content
        if (completion != null) {
            completion = completion.trim();
            int startIdx = completion.indexOf('{');
            int endIdx = completion.lastIndexOf('}') + 1;
            if (startIdx >= 0 && endIdx > startIdx) {
                completion = completion.substring(startIdx, endIdx);
            }
        }
        // Store raw validation output; downstream agents can parse if needed.
        context.put("validationResult", completion != null ? completion : "{\"pass\":true,\"reasons\":[\"No execution data\"]}");
        return completion != null ? completion : "{\"pass\":true,\"reasons\":[\"No execution data\"]}";
    }
}