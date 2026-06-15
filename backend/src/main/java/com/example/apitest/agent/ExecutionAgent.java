package com.example.apitest.agent;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

/**
 * Writes the generated test source to the test tree and executes it via JUnit
 * Platform. The result summary (tests run / failures) is stored in the context.
 */
@Component
public class ExecutionAgent implements LangGraphAgent {

    @Override
    public Object run(Map<String, Object> context) {
        String testCode = (String) context.getOrDefault("generatedTestCode", "");
        if (testCode.isBlank()) {
            context.put("executionResult", "NO_TEST_CODE");
            return "NO_TEST_CODE";
        }
        // Write to target/test-classes/com/example/apitest/generated/SampleApiTest.java
        // Use absolute path to project root for reliable file writing
        Path target = Paths.get(System.getProperty("user.dir"), "src", "test", "java", "com", "example", "apitest", "generated", "SampleApiTest.java");
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, testCode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            context.put("executionResult", "WRITE_FAILED");
            return "WRITE_FAILED";
        }

        // Discover and run the generated test class.
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("com.example.apitest.generated"))
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        TestExecutionSummary summary = listener.getSummary();
        String result = String.format("Executed %d tests, %d failures", summary.getTestsFoundCount(), summary.getTestsFailedCount());
        context.put("executionResult", result);
        return result;
    }
}