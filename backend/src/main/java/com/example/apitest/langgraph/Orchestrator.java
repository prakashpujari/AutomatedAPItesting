package com.example.apitest.langgraph;

import com.example.apitest.agent.*;
import com.example.apitest.config.LlmGatewayConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Orchestrator coordinates the ten agents in the exact order defined by the
 * architecture diagram. This implementation performs a simple sequential run
 * (no external LangGraph library required) - each agent receives the shared
 * mutable {@code context} map and may add new keys for downstream agents.
 */
@Component
public class Orchestrator {
    private final ApiDiscoveryAgent apiDiscoveryAgent;
    private final SpecAnalysisAgent specAnalysisAgent;
    private final TestGenerationAgent testGenerationAgent;
    private final TestDataAgent testDataAgent;
    private final ExecutionAgent executionAgent;
    private final ValidationAgent validationAgent;
    private final FailureAnalysisAgent failureAnalysisAgent;
    private final JiraAgent jiraAgent;
    private final ReportingAgent reportingAgent;
    private final OptimizationAgent optimizationAgent;

    public Orchestrator(ApiDiscoveryAgent apiDiscoveryAgent,
                        SpecAnalysisAgent specAnalysisAgent,
                        TestGenerationAgent testGenerationAgent,
                        TestDataAgent testDataAgent,
                        ExecutionAgent executionAgent,
                        ValidationAgent validationAgent,
                        FailureAnalysisAgent failureAnalysisAgent,
                        JiraAgent jiraAgent,
                        ReportingAgent reportingAgent,
                        OptimizationAgent optimizationAgent) {
        this.apiDiscoveryAgent = apiDiscoveryAgent;
        this.specAnalysisAgent = specAnalysisAgent;
        this.testGenerationAgent = testGenerationAgent;
        this.testDataAgent = testDataAgent;
        this.executionAgent = executionAgent;
        this.validationAgent = validationAgent;
        this.failureAnalysisAgent = failureAnalysisAgent;
        this.jiraAgent = jiraAgent;
        this.reportingAgent = reportingAgent;
        this.optimizationAgent = optimizationAgent;
    }

    /**
     * Executes the full pipeline.
     *
     * @param input user-provided parameters (e.g., {@code apiSources})
     * @return map of all agent outputs keyed by agent name
     */
    public Map<String, Object> run(Map<String, Object> input) {
        Map<String, Object> context = new HashMap<>(input);
        // Sequentially invoke each agent, storing its result under its name.
        Map<String, Object> result = new HashMap<>();
        result.put("apiDiscovery", apiDiscoveryAgent.run(context));
        result.put("specAnalysis", specAnalysisAgent.run(context));
        result.put("testGeneration", testGenerationAgent.run(context));
        result.put("testData", testDataAgent.run(context));
        result.put("execution", executionAgent.run(context));
        result.put("validation", validationAgent.run(context));
        result.put("failureAnalysis", failureAnalysisAgent.run(context));
        result.put("jira", jiraAgent.run(context));
        result.put("reporting", reportingAgent.run(context));
        result.put("optimization", optimizationAgent.run(context));
        // Also expose the final shared context for debugging if needed.
        result.put("finalContext", context);
        return result;
    }
}