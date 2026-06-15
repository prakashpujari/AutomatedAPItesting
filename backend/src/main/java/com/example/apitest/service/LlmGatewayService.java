package com.example.apitest.service;

import com.example.apitest.config.LlmGatewayConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Thin wrapper around the external LLM gateway that the user provides.
 * All agents delegate to this service for model routing and completion.
 *
 * Supports OpenAI-compatible /chat/completions endpoint:
 * {
 *   "model": "<model-id>",
 *   "messages": [{"role": "user", "content": "<prompt>"}]
 * }
 * Returns response with choices[0].message.content.
 */
@Service
public class LlmGatewayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final LlmGatewayConfig config;

    public LlmGatewayService(LlmGatewayConfig config) {
        this.config = config;
    }

    /**
     * Calls the external gateway.
     *
     * @param model   the model identifier (e.g., "llama-3-8b", "qwen", "deepseek-coder")
     * @param prompt  the prompt to send
     * @return the raw completion string (or empty on error)
     */
    public String complete(String model, String prompt) {
        String url = config.getGatewayUrl() + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        // Build request body for OpenAI-compatible chat completions
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyMap, headers);
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object choices = resp.getBody().get("choices");
                if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) ((List<?>) choices).get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    if (message != null) {
                        Object content = message.get("content");
                        if (content != null) {
                            return content.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log in a real app - here we swallow and return empty.
        }
        return "";
    }
}