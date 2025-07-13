package com.watchtower.fakes;

import com.watchtower.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

/**
 * Fake Azure Monitor client
 */
public class AzureMonitorFake {
    private final List<LogEntry> allLogs;
    private final ObjectMapper mapper = new ObjectMapper();
    
    public AzureMonitorFake(Map<String, String> credentials) {
        System.out.println(">>> Azure Monitor authenticated with subscription: " + 
            credentials.get("subscriptionId"));
        this.allLogs = loadLogsFromResource();
        System.out.println(">>> Azure Monitor Fake initialized with " + allLogs.size() + " log entries");
    }
    
    public List<LogEntry> queryLogs(String workspace, String kustoQuery) {
        System.out.println(">>> Azure API: query(workspace=" + workspace + ", query=" + kustoQuery + ")");
        
        // Simplified - just return filtered logs
        return allLogs.stream()
            .filter(log -> kustoQuery.contains("Error") ? log.severity().equals("ERROR") : true)
            .limit(1000)
            .collect(Collectors.toList());
    }
    
    private List<LogEntry> loadLogsFromResource() {
        try (InputStream is = getClass().getResourceAsStream("/azure/monitor-logs.json")) {
            JsonNode root = mapper.readTree(is);
            JsonNode tables = root.get("tables").get(0);
            JsonNode rows = tables.get("rows");
            
            return StreamSupport.stream(rows.spliterator(), false)
                .map(row -> new LogEntry(
                    Instant.parse(row.get(0).asText()), // timestamp
                    row.get(1).asText(), // message
                    row.get(2).asText(), // severity
                    row.get(3).asText()  // source
                ))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Azure logs", e);
        }
    }
}