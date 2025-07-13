package com.watchtower;

import com.watchtower.fakes.AWSCloudWatchFake;
import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - MVP with AWS CloudWatch support only
 * <p>
 * This is our initial implementation - just troubleshooting for now.
 * Notice how everything is hardcoded to AWS.
 * This works great for our MVP, but what happens when customers want GCP support?
 */
public class WatchTowerAgent {
    private final AWSCloudWatchFake awsClient;
    private final LLMFake llm;

    public WatchTowerAgent() {
        // Hardcoded AWS setup - this is our MVP!

        // Step 1: AWS Authentication (in real world, this would be AWS SDK credentials)
        Map<String, Optional<String>> awsCredentials = Map.of(
                "accessKeyId", Optional.ofNullable(System.getenv("AWS_ACCESS_KEY_ID")),
                "secretAccessKey", Optional.ofNullable(System.getenv("AWS_SECRET_ACCESS_KEY")),
                "region", Optional.ofNullable("us-east-1")
        );

        System.out.println(">>> Authenticating with AWS...");
        this.awsClient = new AWSCloudWatchFake(awsCredentials);

        // Step 2: Initialize LLM
        this.llm = new LLMFake();

        System.out.println(">>> WatchTower.AI initialized with AWS CloudWatch support");
    }

    public String troubleshootErrors(String userQuery) {
        System.out.println(">>> Troubleshooting: " + userQuery);

        // Fetch error logs from AWS - hardcoded to payment service
        List<LogEntry> logs = awsClient.filterLogEvents(
                "/aws/payment-service",
                "ERROR",
                1000
        );

        // Convert to string for LLM
        String logData = getString(logs);

        // Use LLM to analyze
        return llm.complete("troubleshoot", userQuery, logData);
    }

    /**
     * Get LLM Friendly String
     */
    private static String getString(List<LogEntry> logs) {
        String logData = logs.stream()
                .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
                .collect(Collectors.joining("\n"));
        return logData;
    }

    // TODO: Customers asking for more analysis types...
    // TODO: What about GCP logs?
    // TODO: How do we handle different auth patterns for different clouds?
}