package com.watchtower.sources;

import com.watchtower.model.LogEntry;
import java.util.List;
import java.util.Map;

/**
 * Common interface for all cloud log sources.
 * Now with capability discovery!
 */
public interface CloudLogSource {
    
    /**
     * Initialize the source with cloud-specific configuration
     */
    void initialize(Map<String, String> config);
    
    /**
     * Fetch logs from this cloud source
     */
    List<LogEntry> fetchLogs(String logicalResource, String filter, int limit);
    
    /**
     * Get the cloud provider name
     */
    String getCloudProvider();
    
    /**
     * NEW: Discover what this source can do
     * @return The capabilities of this source
     */
    SourceCapabilities getCapabilities();
}