package com.watchtower.sources;

import com.watchtower.fakes.GCPLoggingFake;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * GCP Cloud Logging implementation of CloudLogSource
 */
@Slf4j
public class GCPLogSource implements CloudLogSource {
    
    private GCPLoggingFake client;
    
    @Override
    public void initialize(Map<String, String> config) {
        log.info("Initializing GCP Cloud Logging source");
        
        String serviceAccountPath = config.get("serviceAccountPath");
        this.client = new GCPLoggingFake(serviceAccountPath);
    }
    
    @Override
    public List<LogEntry> fetchLogs(String logicalResource, String filter, int limit) {
        // Translate logical names to GCP log names
        String logName = translateToGCPLogName(logicalResource);
        String gcpFilter = translateToGCPFilter(filter);
        return client.listLogEntries(logName, gcpFilter, limit);
    }
    
    @Override
    public List<Metric> fetchMetrics(String logicalResource, String metricName, String timeRange) {
        // GCP uses different metric naming
        String resourceType = translateToGCPResource(logicalResource);
        String gcpMetricType = translateToGCPMetric(metricName);
        
        // For demo, return fake metrics
        return generateFakeMetrics(resourceType, gcpMetricType, timeRange);
    }
    
    @Override
    public String getCloudProvider() {
        return "GCP";
    }
    
    private String translateToGCPLogName(String logicalResource) {
        return switch (logicalResource) {
            case "payment-service" -> "projects/my-gcp-project/logs/payment-service";
            case "user-service" -> "projects/my-gcp-project/logs/user-service";
            default -> "projects/my-gcp-project/logs/" + logicalResource;
        };
    }
    
    private String translateToGCPFilter(String filter) {
        return switch (filter) {
            case "ERROR" -> "severity=\"ERROR\"";
            case "WARN" -> "severity=\"WARNING\"";
            case "INFO" -> "severity=\"INFO\"";
            default -> "severity=\"" + filter + "\"";
        };
    }
    
    private String translateToGCPResource(String logicalResource) {
        return switch (logicalResource) {
            case "payment-service" -> "gce_instance";
            case "user-service" -> "gce_instance";
            default -> "global";
        };
    }
    
    private String translateToGCPMetric(String metricName) {
        return switch (metricName) {
            case "error_rate" -> "compute.googleapis.com/instance/cpu/usage_time";
            case "cpu_usage" -> "compute.googleapis.com/instance/cpu/utilization";
            case "request_count" -> "loadbalancing.googleapis.com/https/request_count";
            default -> metricName;
        };
    }
    
    private List<Metric> generateFakeMetrics(String resourceType, String metricName, String timeRange) {
        // Generate some fake metrics for demo
        List<Metric> metrics = new ArrayList<>();
        Instant now = Instant.now();
        
        // Generate data points based on time range
        int dataPoints = switch (timeRange) {
            case "1h" -> 12;   // 5-minute intervals
            case "24h" -> 24;  // Hourly
            case "7d" -> 7;    // Daily
            default -> 10;
        };
        
        for (int i = 0; i < dataPoints; i++) {
            metrics.add(new Metric(
                now.minus(i * 5, ChronoUnit.MINUTES),
                metricName,
                Math.random() * 100,  // Random value for demo
                "Count",
                Map.of("resource_type", resourceType)
            ));
        }
        
        return metrics;
    }
}