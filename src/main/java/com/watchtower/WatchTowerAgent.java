package com.watchtower;

import com.watchtower.sources.*;
import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Now with discovery and multiple personas!
 */
@Slf4j
public class WatchTowerAgent {
    private final Map<String, CloudLogSource> sources;
    private final LLMFake llm;
    
    public WatchTowerAgent() {
        this.sources = initializeCloudSources();
        this.llm = new LLMFake();
        
        // Show what each source can do
        System.out.println(">>> WatchTower.AI initialized with capabilities:");
        sources.forEach((name, source) -> {
            var caps = source.getCapabilities();
            System.out.printf("  %s: %s (operations: %s)%n", 
                name, caps.getDescription(), caps.getSupportedOperations());
        });
    }
    
    private Map<String, CloudLogSource> initializeCloudSources() {
        // AWS Source
        CloudLogSource awsSource = new AWSLogSource();
        awsSource.initialize(Map.of(
            "AWS_ACCESS_KEY_ID", getConfigValue("AWS_ACCESS_KEY_ID"),
            "AWS_SECRET_ACCESS_KEY", getConfigValue("AWS_SECRET_ACCESS_KEY")
        ));
        
        // GCP Source
        CloudLogSource gcpSource = new GCPLogSource();
        gcpSource.initialize(Map.of(
            "GOOGLE_APPLICATION_CREDENTIALS", getConfigValue("GOOGLE_APPLICATION_CREDENTIALS")
        ));
        
        // Azure Source (NEW!)
        CloudLogSource azureSource = new AzureLogSource();
        azureSource.initialize(Map.of(
            "AZURE_SUBSCRIPTION_ID", getConfigValue("AZURE_SUBSCRIPTION_ID"),
            "AZURE_TENANT_ID", getConfigValue("AZURE_TENANT_ID"),
            "AZURE_CLIENT_ID", getConfigValue("AZURE_CLIENT_ID"),
            "AZURE_CLIENT_SECRET", getConfigValue("AZURE_CLIENT_SECRET")
        ));
        
        return Map.of(
            "AWS", awsSource,
            "GCP", gcpSource,
            "AZURE", azureSource
        );
    }
    
    private String getConfigValue(String key) {
        // Try system property first (for tests), then environment variable
        String value = System.getProperty(key);
        if (value != null) return value;
        
        value = System.getenv(key);
        return value != null ? value : ""; // Return empty string instead of null
    }
    
    public String troubleshootErrors(String userQuery, String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "I cannot troubleshoot errors for " + cloudProvider + " as it's not a configured provider.";
        }
        
        var capabilities = source.getCapabilities();
        
        // Check if we can fetch logs
        if (!capabilities.getSupportedOperations().contains("fetchLogs")) {
            return String.format("[%s] I cannot troubleshoot errors as this provider doesn't support fetching logs.", 
                cloudProvider);
        }
        
        List<LogEntry> logs = source.fetchLogs("payment-service", "ERROR", 1000);
        
        String logData = logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
        
        return llm.complete("troubleshoot", 
            String.format("[%s] %s", cloudProvider, userQuery), 
            logData);
    }
    
    public String generateSummary(String timeRange, String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "I cannot generate a summary for " + cloudProvider + " as it's not a configured provider.";
        }
        
        var capabilities = source.getCapabilities();
        
        // Check what data we can gather
        boolean canFetchLogs = capabilities.getSupportedOperations().contains("fetchLogs");
        boolean canFetchMetrics = capabilities.getSupportedOperations().contains("fetchMetrics");
        boolean supportsTimeRange = capabilities.getSupportedTimeRanges().contains(timeRange);
        
        if (!canFetchLogs && !canFetchMetrics) {
            return String.format("[%s] I cannot generate a summary as this provider doesn't support fetching logs or metrics.", 
                cloudProvider);
        }
        
        if (!supportsTimeRange) {
            // Gracefully fall back to supported time range
            String fallbackRange = capabilities.getSupportedTimeRanges().isEmpty() ? 
                "24h" : capabilities.getSupportedTimeRanges().get(0);
            timeRange = fallbackRange;
        }
        
        String logData = "";
        if (canFetchLogs) {
            List<LogEntry> logs = source.fetchLogs("payment-service", "INFO", 5000);
            logData = formatLogsForSummary(logs);
        }
        
        // Note what data is available for the LLM
        String context = String.format(
            "[%s] Generate summary for %s (logs: %s, metrics: %s)", 
            cloudProvider, timeRange, canFetchLogs, canFetchMetrics
        );
        
        return llm.complete("summary", context, logData);
    }
    
    public String detectAnomalies(String baseline, String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "I cannot detect anomalies for " + cloudProvider + " as it's not a configured provider.";
        }
        
        var capabilities = source.getCapabilities();
        
        // Check what we need for anomaly detection
        boolean canFetchLogs = capabilities.getSupportedOperations().contains("fetchLogs");
        boolean canFetchMetrics = capabilities.getSupportedOperations().contains("fetchMetrics");
        boolean hasLongTermData = capabilities.getSupportedTimeRanges().stream()
            .anyMatch(range -> range.equals("30d") || range.equals("90d"));
        
        if (!canFetchLogs && !canFetchMetrics) {
            return String.format("[%s] I cannot detect anomalies without access to logs or metrics.", 
                cloudProvider);
        }
        
        if (!hasLongTermData) {
            // Warn but proceed with limited data
            String availableRange = capabilities.getSupportedTimeRanges().isEmpty() ? 
                "24h" : capabilities.getSupportedTimeRanges().get(capabilities.getSupportedTimeRanges().size() - 1);
            
            return llm.complete("anomaly",
                String.format("[%s] Detecting anomalies with limited historical data (%s only). Baseline: %s", 
                    cloudProvider, availableRange, baseline),
                "Limited data available"
            );
        }
        
        String logData = "";
        if (canFetchLogs) {
            List<LogEntry> logs = source.fetchLogs("payment-service", "INFO", 10000);
            logData = formatLogsForAnomalyDetection(logs);
        }
        
        return llm.complete("anomaly",
            String.format("[%s] Detect anomalies. Baseline: %s", cloudProvider, baseline),
            logData);
    }
    
    // Discovery method - let callers know what's available
    public Map<String, SourceCapabilities> discoverCapabilities() {
        return sources.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getCapabilities()
            ));
    }
    
    private String formatLogsForSummary(List<LogEntry> logs) {
        // Group by severity for summary
        return logs.stream()
            .collect(Collectors.groupingBy(LogEntry::severity))
            .entrySet().stream()
            .map(e -> String.format("%s: %d entries", e.getKey(), e.getValue().size()))
            .collect(Collectors.joining("\n"));
    }
    
    private String formatLogsForAnomalyDetection(List<LogEntry> logs) {
        // Time-based grouping for anomaly detection
        return "Log patterns over time..."; // Simplified for demo
    }
}