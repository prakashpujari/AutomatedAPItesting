package com.example.apitest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Jira integration. Environment variables are expected:
 *   JIRA_URL   - base URL of the Jira instance (e.g. https://mycompany.atlassian.net)
 *   JIRA_USER  - username / email used for basic auth
 *   JIRA_TOKEN - API token (generated in Jira UI)
 *   JIRA_PROJECT - Jira project key where tickets will be created (default "API")
 */
@Configuration
public class JiraConfig {

    @Value("${JIRA_URL:}")
    private String jiraUrl;

    @Value("${JIRA_USER:}")
    private String jiraUser;

    @Value("${JIRA_TOKEN:}")
    private String jiraToken;

    @Value("${JIRA_PROJECT:API}")
    private String jiraProject;

    public String getJiraUrl() {
        return jiraUrl;
    }

    public String getJiraUser() {
        return jiraUser;
    }

    public String getJiraToken() {
        return jiraToken;
    }

    public String getJiraProject() {
        return jiraProject;
    }
}