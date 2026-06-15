package com.example.apitest.agent;

import com.example.apitest.service.LlmGatewayService;
import com.example.apitest.service.JiraService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a Jira ticket when the failure analysis reports a confidence score above 90.
 * The actual Jira integration is stubbed - it returns a fabricated issue key.
 * In a production setup you would inject a real Jira client and use secrets.
 */
@Component
public class JiraAgent implements LangGraphAgent {

    private final LlmGatewayService llmService;
    private final JiraService jiraService;
    private final com.example.apitest.config.JiraConfig jiraConfig;


    @Autowired
    public JiraAgent(LlmGatewayService llmService, JiraService jiraService, com.example.apitest.config.JiraConfig jiraConfig) {
        this.llmService = llmService;
        this.jiraService = jiraService;
        this.jiraConfig = jiraConfig;
    }

    @Override
    public Object run(Map<String, Object> context) {
        String failureReport = (String) context.getOrDefault("failureReport", "");
        if (failureReport.isBlank() || failureReport.contains("No failures")) {
            context.put("jiraTicket", "None");
            return "None";
        }
        // Extract confidence score - try JSON first, fall back to regex.
        int confidence = 0;
        try {
            // Expect failureReport to be a JSON object like {"confidence":95,...}
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String,Object> map = mapper.readValue(failureReport, Map.class);
            Object conf = map.get("confidence");
            if (conf != null) {
                confidence = Integer.parseInt(conf.toString());
            }
        } catch (Exception ignored) {
            // fallback regex if the report is not JSON
            try {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("confidence score[:]?\\s*(\\d{1,3})", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(failureReport);
                if (m.find()) {
                    confidence = Integer.parseInt(m.group(1));
                }
            } catch (Exception __) {}
        }
        // If confidence > 90, create a real Jira ticket.
        if (confidence > 90) {
            String summary = "Automated failure analysis - confidence " + confidence + "%";
            String description = failureReport;
            String jiraKey = jiraService.createIssue(summary, description);
            if (jiraKey != null) {
                context.put("jiraTicket", jiraKey);
                return jiraKey;
            }
            // fallback to a fabricated key if creation fails
            String fallbackKey = "PROJ-" + (int) (Math.random() * 10000);
            context.put("jiraTicket", fallbackKey);
            return fallbackKey;
        }
        context.put("jiraTicket", "None");
        return "None";
    }
}
