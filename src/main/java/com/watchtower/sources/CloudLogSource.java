package com.watchtower.sources;

import com.watchtower.model.LogEntry;
import java.util.List;
import java.util.Map;

/**
 * Common interface for all cloud log sources.
 * This abstraction hides cloud-specific implementation details.
 */
public interface CloudLogSource {
    
    /**
     * Initialize the source with cloud-specific configuration
     * @param config Cloud-specific config (credentials, region, project, etc.)
     */
    void initialize(Map<String, String> config);
    
    /**
     * Fetch logs from this cloud source
     * @param logicalResource The logical resource name (e.g., "payment-service")
     * @param filter The filter to apply (e.g., "ERROR", "WARN")
     * @param limit Maximum number of logs to return
     * @return List of log entries
     */
    List<LogEntry> fetchLogs(String logicalResource, String filter, int limit);
    
    /**
     * Get the cloud provider name
     * @return Provider name (AWS, GCP, AZURE, etc.)
     */
    String getCloudProvider();
}