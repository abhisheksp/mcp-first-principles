package com.watchtower;

import com.watchtower.sources.CloudLogSource;
import com.watchtower.sources.AWSLogSource;
import com.watchtower.sources.GCPLogSource;
import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Now with clean interface pattern!
 * 
 * Notice how much cleaner this is:
 * - No more if-else chains
 * - Easy to add new cloud providers
 * - Constructor is manageable again
 */
public class WatchTowerAgent {
    private final Map<String, CloudLogSource> sources;
    private final LLMFake llm;
    
    public WatchTowerAgent() {
        // Initialize all cloud sources
        this.sources = initializeCloudSources();
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized with cloud providers: " + 
            sources.keySet());
    }
    
    private Map<String, CloudLogSource> initializeCloudSources() {
        // AWS Source
        CloudLogSource awsSource = new AWSLogSource();
        awsSource.initialize(Map.of(
            "AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID") != null ? System.getenv("AWS_ACCESS_KEY_ID") : "",
            "AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY") != null ? System.getenv("AWS_SECRET_ACCESS_KEY") : "",
            "AWS_REGION", "us-east-1"
        ));
        
        // GCP Source
        CloudLogSource gcpSource = new GCPLogSource();
        gcpSource.initialize(Map.of(
            "GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null ? System.getenv("GOOGLE_APPLICATION_CREDENTIALS") : ""
        ));
        
        // Return immutable map of sources
        return Map.of(
            "AWS", awsSource,
            "GCP", gcpSource
        );
    }
    
    public String troubleshootErrors(String userQuery, String cloudProvider) {
        System.out.println(">>> Troubleshooting on " + cloudProvider + ": " + userQuery);
        
        // Clean: No more if-else or switch statements!
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            throw new IllegalArgumentException("Unsupported cloud provider: " + cloudProvider);
        }
        
        // Use logical resource name - no cloud-specific knowledge here!
        List<LogEntry> logs = source.fetchLogs("payment-service", "ERROR", 1000);
        
        // Convert to string for LLM
        String logData = logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
        
        return llm.complete("troubleshoot", 
            String.format("[%s] %s", cloudProvider, userQuery), 
            logData);
    }
    
    // Clean method to check available providers
    public List<String> getAvailableProviders() {
        return List.copyOf(sources.keySet());
    }
}