package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Analyzes failures (if any) using the Groq model and produces a
 * root-cause analysis (RCA) report.
 */
@Component
public class FailureAnalysisAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;

    @Autowired
    public FailureAnalysisAgent(LlmGatewayService llmService) {
        this.llmService = llmService;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String validation = (String) context.getOrDefault("validationResult", "{}");
        boolean pass = false;
        try {
            if (validation.contains("\"pass\":true") || validation.contains("\"pass\": true")) {
                String msg = "No failures detected - nothing to analyse.";
                context.put("failureReport", msg);
                return msg;
            }
        } catch (Exception e) {
            // Continue with analysis
        }
        // Gather relevant data for analysis - executionResult and maybe logs.
        String execResult = (String) context.getOrDefault("executionResult", "");
        String prompt = """
            You are a senior failure-analysis engineer. Given the execution result and any error messages, produce a Root-Cause Analysis (RCA) report that includes:

            - Root cause description (what went wrong and why).
            - Likely fix (concise code or configuration change suggestion).
            - Severity (high/medium/low) based on impact on production.
            - Business impact (e.g., blocked checkout, data loss, SLA breach).
            - Confidence score (0-100) indicating how certain you are of the analysis.
            - Optional remediation steps (brief checklist).

            Return the report as a single JSON object without markdown fences with keys: rootCause, fix, severity, impact, confidence, steps.
            """;
        String completion = llmService.complete("llama-3.1-8b-instant", prompt + "\nExecution Result:\n" + execResult);
        // Extract JSON content
        if (completion != null) {
            completion = completion.trim();
            int startIdx = completion.indexOf('{');
            int endIdx = completion.lastIndexOf('}') + 1;
            if (startIdx >= 0 && endIdx > startIdx) {
                completion = completion.substring(startIdx, endIdx);
            }
        }
        context.put("failureReport", completion != null ? completion : "{}");
        return completion != null ? completion : "{}";
    }
}