package com.example.apitest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for routing requests to external LLM services.
 *
 * The platform does not host the LLMs directly. Instead it forwards
 * model-specific calls to an existing gateway service (provided by the user).
 *
 * Environment variables expected:
 *   LLM_GATEWAY_URL   - Base URL of the gateway (e.g. https://llm.example.com/api)
 *   LLM_API_KEY       - Authentication token for the gateway
 */
@Configuration
public class LlmGatewayConfig {

    @Value("${LLM_GATEWAY_URL:}")
    private String gatewayUrl;

    @Value("${LLM_API_KEY:}")
    private String apiKey;

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}