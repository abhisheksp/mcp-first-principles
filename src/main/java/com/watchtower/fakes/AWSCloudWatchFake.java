package com.watchtower.fakes;

import com.watchtower.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Fake AWS CloudWatch client that reads from test resources
 * Simulates the AWS CloudWatch Logs API
 */
public class AWSCloudWatchFake {
    private final List<LogEntry> allLogs;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Optional<String>> credentials;

    public AWSCloudWatchFake(Map<String, Optional<String>> credentials) {
        // Simulate AWS authentication
        System.out.println(">>> AWS credentials validated successfully");
        this.credentials = credentials;

        // Load fake log data
        this.allLogs = loadLogsFromResource();
        System.out.println(">>> AWS CloudWatch Fake initialized with " + allLogs.size() + " log entries");
        System.out.println(">>> Connected to region: " + credentials.getOrDefault("region", Optional.of("us-east-1")).get());
    }


    /**
     * Simulates the FilterLogEvents API call
     */
    public List<LogEntry> filterLogEvents(String logGroup, String filterPattern, int limit) {
        System.out.printf(">>> AWS API: FilterLogEvents(logGroup=%s, filter=%s, limit=%d)%n",
            logGroup, filterPattern, limit);

        return allLogs.stream()
            .filter(log -> matchesLogGroup(log, logGroup))
            .filter(log -> matchesFilter(log, filterPattern))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private boolean matchesLogGroup(LogEntry log, String logGroup) {
        // Simple matching - in real AWS, this would be more complex
        return log.source().contains(logGroup.replace("/aws/", ""));
    }

    private boolean matchesFilter(LogEntry log, String filterPattern) {
        if (filterPattern == null || filterPattern.isEmpty()) {
            return true;
        }
        // Simple filter matching
        return log.message().contains(filterPattern) ||
               log.severity().equalsIgnoreCase(filterPattern);
    }

    private List<LogEntry> loadLogsFromResource() {
        List<LogEntry> logs = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/aws/cloudwatch-logs.json")) {
            JsonNode root = mapper.readTree(is);
            JsonNode events = root.get("events");

            for (JsonNode event : events) {
                long timestamp = event.get("timestamp").asLong();
                String message = event.get("message").asText();
                String logStream = event.get("logStreamName").asText();

                // Extract severity from message
                String severity = "INFO";
                if (message.contains("ERROR")) severity = "ERROR";
                else if (message.contains("WARN")) severity = "WARN";

                logs.add(new LogEntry(
                    Instant.ofEpochMilli(timestamp),
                    message,
                    severity,
                    logStream
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AWS logs from resource", e);
        }
        return logs;
    }
}