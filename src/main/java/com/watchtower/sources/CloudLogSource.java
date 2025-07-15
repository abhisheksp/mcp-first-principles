package com.watchtower.sources;

import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;
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
     * NEW: Fetch metrics from this cloud source
     * @param logicalResource The logical resource name (e.g., "payment-service")
     * @param metricName The metric to fetch (e.g., "cpu_usage", "error_rate")
     * @param timeRange Time range for metrics (e.g., "1h", "24h", "7d")
     * @return List of metric data points
     */
    List<Metric> fetchMetrics(String logicalResource, String metricName, String timeRange);
    
    /**
     * Get the cloud provider name
     * @return Provider name (AWS, GCP, AZURE, etc.)
     */
    String getCloudProvider();
}