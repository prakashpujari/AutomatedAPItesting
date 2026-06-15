package com.example.apitest.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.example.apitest.config.JiraConfig;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Wrapper around the Atlassian Jira REST client.
 *
 * This service creates a simple issue (Bug) using the configuration supplied
 * via environment variables (JIRA_URL, JIRA_USER, JIRA_TOKEN, JIRA_PROJECT).
 */
@Service
public class JiraService {

    private final JiraConfig config;

    public JiraService(JiraConfig config) {
        this.config = config;
    }

    /**
     * Creates a Jira issue and returns the issue key.
     *
     * @param summary     short title for the ticket
     * @param description detailed description (can contain the failure report)
     * @return the created issue key, or null if creation fails
     */
    public String createIssue(String summary, String description) {
        // Validate configuration - if any required value is missing we skip creation.
        if (config.getJiraUrl().isBlank() || config.getJiraUser().isBlank() || config.getJiraToken().isBlank()) {
            return null;
        }
        try (JiraRestClient client = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(new URI(config.getJiraUrl()), config.getJiraUser(), config.getJiraToken())) {
            IssueInputBuilder builder = new IssueInputBuilder(config.getJiraProject(), 1L); // 1L = default issue type "Bug"
            builder.setSummary(summary);
            builder.setDescription(description);
            BasicIssue issue = client.getIssueClient().createIssue(builder.build()).claim();
            return issue.getKey();
        } catch (Exception e) {
            // In a real system you would log the exception.
            return null;
        }
    }
}