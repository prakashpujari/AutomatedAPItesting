package com.example.apitest.agent;

import java.util.Map;

/**
 * Common contract for all LangGraph agents in the platform.
 *
 * Each agent receives a mutable {@code context} map that contains data produced by
 * previous agents. The agent may read from and write to the map. The returned
 * value is stored under the agent's name in the workflow output.
 */
public interface LangGraphAgent {
    /**
     * Execute the agent's logic.
     *
     * @param context shared execution context
     * @return result object (can be any serialisable type)
     */
    Object run(Map<String, Object> context);
}
