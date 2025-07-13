package com.watchtower.fakes;

import com.watchtower.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;

/**
 * Fake GCP Logging client that reads from test resources
 * Simulates the Google Cloud Logging API
 * 
 * Notice how different this is from AWS:
 * - Different auth (service account JSON)
 * - Different API structure
 * - Different log format
 */
@Slf4j
public class GCPLoggingFake {
    private final List<LogEntry> allLogs;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    
    public GCPLoggingFake(String serviceAccountPath) {
        // Simulate GCP authentication
        System.out.println(">>> GCP service account validated: " + serviceAccountPath);
        this.projectId = "my-gcp-project";
        
        // Load fake log data
        this.allLogs = loadLogsFromResource();
        System.out.println(">>> GCP Logging Fake initialized with " + allLogs.size() + " log entries");
        System.out.println(">>> Connected to project: " + projectId);
    }
    
    /**
     * Simulates the GCP entries.list API call
     * Notice the different parameter names and patterns vs AWS
     */
    public List<LogEntry> listLogEntries(String logName, String filter, int pageSize) {
        System.out.printf(">>> GCP API: entries.list(logName=%s, filter=%s, pageSize=%d)%n", 
            logName, filter, pageSize);
        
        return allLogs.stream()
            .filter(log -> matchesLogName(log, logName))
            .filter(log -> matchesFilter(log, filter))
            .limit(pageSize)
            .collect(ImmutableList.toImmutableList());
    }
    
    private boolean matchesLogName(LogEntry log, String logName) {
        // GCP uses different naming: projects/PROJECT/logs/LOG_ID
        return log.source().contains(logName.replace("projects/" + projectId + "/logs/", ""));
    }
    
    private boolean matchesFilter(LogEntry log, String filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        // GCP uses different filter syntax
        if (filter.contains("severity")) {
            String severity = filter.split("=")[1].trim().replace("\"", "");
            return log.severity().equalsIgnoreCase(severity);
        }
        return log.message().contains(filter);
    }
    
    private List<LogEntry> loadLogsFromResource() {
        try (InputStream is = getClass().getResourceAsStream("/gcp/stackdriver-logs.json")) {
            JsonNode root = mapper.readTree(is);
            JsonNode entries = root.get("entries");
            
            return StreamSupport.stream(entries.spliterator(), false)
                .map(entry -> {
                    String timestamp = entry.get("timestamp").asText();
                    String message = entry.get("textPayload").asText();
                    String severity = entry.get("severity").asText();
                    String logName = entry.get("logName").asText();
                    
                    return new LogEntry(
                        Instant.parse(timestamp),
                        message,
                        severity,
                        logName
                    );
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load GCP logs from resource", e);
        }
    }
}