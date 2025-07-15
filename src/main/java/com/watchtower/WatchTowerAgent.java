package com.watchtower;

import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;
import com.watchtower.sources.AWSLogSource;
import com.watchtower.sources.CloudLogSource;
import com.watchtower.sources.GCPLogSource;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Phase 3: Clean abstraction with multiple operations
 * 
 * Now we have a clean CloudLogSource interface that hides cloud-specific details.
 * Both fetchLogs and fetchMetrics are available, but WE still decide what to call and when.
 * 
 * The hidden problem: We're still hardcoding the decision of what data to fetch!
 */
@Slf4j
public class WatchTowerAgent {
    
    public final Map<String, CloudLogSource> sources;
    private final LLMFake llm;
    
    public WatchTowerAgent() {
        log.info(">>> Initializing WatchTower.AI with clean abstractions...");
        
        this.sources = new HashMap<>();
        this.llm = new LLMFake();
        
        // Initialize AWS source
        CloudLogSource awsSource = new AWSLogSource();
        awsSource.initialize(Map.of(
            "accessKeyId", System.getenv("AWS_ACCESS_KEY_ID") != null ? System.getenv("AWS_ACCESS_KEY_ID") : "fake-key",
            "secretAccessKey", System.getenv("AWS_SECRET_ACCESS_KEY") != null ? System.getenv("AWS_SECRET_ACCESS_KEY") : "fake-secret",
            "region", "us-east-1"
        ));
        sources.put("AWS", awsSource);
        
        // Initialize GCP source
        CloudLogSource gcpSource = new GCPLogSource();
        gcpSource.initialize(Map.of(
            "serviceAccountPath", System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null ? System.getenv("GOOGLE_APPLICATION_CREDENTIALS") : "fake-path"
        ));
        sources.put("GCP", gcpSource);
        
        log.info(">>> WatchTower.AI initialized with {} cloud sources", sources.size());
    }
    
    public String troubleshootErrors(String userQuery, String cloudProvider) {
        log.info(">>> Troubleshooting on {}: {}", cloudProvider, userQuery);
        
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "I cannot troubleshoot errors for " + cloudProvider + " as it's not a configured provider.";
        }
        
        // WE decide to fetch logs first
        List<LogEntry> logs = source.fetchLogs("payment-service", "ERROR", 1000);
        
        // WE decide to also check metrics
        List<Metric> errorRates = source.fetchMetrics("payment-service", "error_rate", "1h");
        
        // WE manually combine the data
        String logData = logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
            
        String metricData = errorRates.stream()
            .map(m -> String.format("[%s] %s: %.2f %s", m.getTimestamp(), m.getName(), m.getValue(), m.getUnit()))
            .collect(Collectors.joining("\n"));
        
        String combinedContext = String.format(
            "User Query: %s\n\nError Logs:\n%s\n\nError Rate Metrics:\n%s",
            userQuery, logData, metricData
        );
        
        // Send everything to LLM as combined context
        return llm.complete("troubleshoot", userQuery, combinedContext);
    }
    
    public List<String> getAvailableProviders() {
        return new ArrayList<>(sources.keySet());
    }
    
    public String getProviderInfo(String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "Provider " + cloudProvider + " is not configured.";
        }
        return "Provider: " + source.getCloudProvider() + " (configured and ready)";
    }
}