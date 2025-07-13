package com.watchtower.sources;

import com.watchtower.fakes.GCPLoggingFake;
import com.watchtower.model.LogEntry;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * GCP Logging adapter implementing our common interface
 */
@RequiredArgsConstructor
public class GCPLogSource implements CloudLogSource {
    private GCPLoggingFake client;
    
    @Override
    public void initialize(Map<String, String> config) {
        System.out.println(">>> Initializing GCP Log Source...");
        
        // GCP-specific credential setup
        String serviceAccountPath = config.getOrDefault("GOOGLE_APPLICATION_CREDENTIALS", "");
        
        this.client = new GCPLoggingFake(serviceAccountPath);
    }
    
    @Override
    public List<LogEntry> fetchLogs(String logicalResource, String filter, int limit) {
        // Translate logical resource to GCP-specific format
        String gcpLogName = translateToGCPLogName(logicalResource);
        
        // Adapt filter format for GCP
        String gcpFilter = translateFilter(filter);
        
        // Adapt our interface to GCP-specific API
        return client.listLogEntries(gcpLogName, gcpFilter, limit);
    }
    
    private String translateToGCPLogName(String logicalResource) {
        // Map logical names to GCP log names
        String projectId = "my-gcp-project"; // Would come from config
        return switch (logicalResource) {
            case "payment-service" -> "projects/" + projectId + "/logs/payment-service";
            case "user-service" -> "projects/" + projectId + "/logs/user-service";
            case "order-service" -> "projects/" + projectId + "/logs/order-service";
            default -> "projects/" + projectId + "/logs/" + logicalResource;
        };
    }
    
    private String translateFilter(String filter) {
        // GCP uses different filter syntax
        return switch (filter) {
            case "ERROR" -> "severity=\"ERROR\"";
            case "WARN" -> "severity=\"WARNING\"";
            case "INFO" -> "severity=\"INFO\"";
            default -> filter; // Pass through unknown filters
        };
    }
    
    @Override
    public String getCloudProvider() {
        return "GCP";
    }
}