package com.watchtower;

import com.watchtower.sources.*;
import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Showing the system prompt pattern for personas
 * 
 * Note: This is separate from the transport multiplication problem.
 * The agent uses these prompts regardless of how sources are accessed.
 */
@Slf4j
public class WatchTowerAgent {
    private final Map<String, CloudLogSource> sources;
    private final LLMFake llm;
    
    // The "secret sauce" - carefully crafted system prompts for each persona
    // This is typically the proprietary/differentiating part of AI agents
    private static final Map<String, String> PERSONA_SYSTEM_PROMPTS = Map.of(
        "troubleshoot", """
            You are an expert Site Reliability Engineer with 15 years of experience.
            When analyzing logs for issues:
            1. Identify the specific error patterns and their frequency
            2. Determine the root cause based on error messages and timing
            3. Assess the blast radius and user impact
            4. Provide immediate mitigation steps
            5. Suggest long-term fixes
            
            Format your response as:
            ISSUE SUMMARY: [Brief description]
            ROOT CAUSE: [Technical explanation]
            IMPACT: [Who/what is affected]
            IMMEDIATE ACTION: [Steps to fix now]
            LONG-TERM FIX: [Permanent solution]
            """,
            
        "summary", """
            You are a technical analyst preparing executive dashboards.
            When summarizing logs:
            1. Calculate key metrics (error rate, throughput, latency)
            2. Identify trends and patterns
            3. Highlight significant events
            4. Compare to historical baselines
            
            Format your response as:
            PERIOD: [Time range]
            KEY METRICS: [Bulleted list]
            TRENDS: [What changed]
            NOTABLE EVENTS: [Important occurrences]
            RECOMMENDATIONS: [Action items]
            """,
            
        "anomaly", """
            You are a security analyst specializing in threat detection.
            When detecting anomalies:
            1. Establish normal baseline behavior
            2. Identify deviations from baseline
            3. Classify anomaly severity (low/medium/high/critical)
            4. Assess security implications
            5. Recommend investigation steps
            
            Format your response as:
            BASELINE: [Normal behavior]
            ANOMALIES DETECTED: [List of deviations]
            SEVERITY: [Risk level]
            SECURITY IMPLICATIONS: [Potential threats]
            RECOMMENDED ACTIONS: [Next steps]
            """
    );
    
    public WatchTowerAgent() {
        this.sources = initializeCloudSources();
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized");
        System.out.println(">>> Available personas with specialized prompts: " + 
            PERSONA_SYSTEM_PROMPTS.keySet());
    }
    
    // Existing initialization code...
    private Map<String, CloudLogSource> initializeCloudSources() {
        CloudLogSource awsSource = new AWSLogSource();
        awsSource.initialize(Map.of(
            "AWS_ACCESS_KEY_ID", getConfigValue("AWS_ACCESS_KEY_ID"),
            "AWS_SECRET_ACCESS_KEY", getConfigValue("AWS_SECRET_ACCESS_KEY")
        ));
        
        CloudLogSource gcpSource = new GCPLogSource();
        gcpSource.initialize(Map.of(
            "GOOGLE_APPLICATION_CREDENTIALS", getConfigValue("GOOGLE_APPLICATION_CREDENTIALS")
        ));
        
        CloudLogSource azureSource = new AzureLogSource();
        azureSource.initialize(Map.of(
            "AZURE_SUBSCRIPTION_ID", getConfigValue("AZURE_SUBSCRIPTION_ID"),
            "AZURE_TENANT_ID", getConfigValue("AZURE_TENANT_ID"),
            "AZURE_CLIENT_ID", getConfigValue("AZURE_CLIENT_ID"),
            "AZURE_CLIENT_SECRET", getConfigValue("AZURE_CLIENT_SECRET")
        ));
        
        return Map.of("AWS", awsSource, "GCP", gcpSource, "AZURE", azureSource);
    }
    
    private String getConfigValue(String key) {
        // Try system property first (for tests), then environment variable
        String value = System.getProperty(key);
        if (value != null) return value;
        
        value = System.getenv(key);
        return value != null ? value : ""; // Return empty string instead of null
    }
    
    // Each persona method uses its specialized system prompt
    public String troubleshootErrors(String userQuery, String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "Unknown provider: " + cloudProvider;
        }
        
        // Fetch appropriate data for troubleshooting
        List<LogEntry> logs = source.fetchLogs("payment-service", "ERROR", 1000);
        String logData = formatLogs(logs);
        
        // Use the troubleshooting system prompt
        String systemPrompt = PERSONA_SYSTEM_PROMPTS.get("troubleshoot");
        return llm.completeWithSystemPrompt(systemPrompt, userQuery, logData);
    }
    
    public String generateSummary(String timeRange, String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "Unknown provider: " + cloudProvider;
        }
        
        // Fetch appropriate data for summary
        List<LogEntry> logs = source.fetchLogs("payment-service", "INFO", 5000);
        String logData = formatLogs(logs);
        
        // Use the summary system prompt
        String systemPrompt = PERSONA_SYSTEM_PROMPTS.get("summary");
        return llm.completeWithSystemPrompt(systemPrompt, 
            "Generate summary for " + timeRange, logData);
    }
    
    public String detectAnomalies(String baseline, String cloudProvider) {
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "Unknown provider: " + cloudProvider;
        }
        
        // Fetch appropriate data for anomaly detection
        List<LogEntry> logs = source.fetchLogs("payment-service", "INFO", 10000);
        String logData = formatLogs(logs);
        
        // Use the anomaly detection system prompt
        String systemPrompt = PERSONA_SYSTEM_PROMPTS.get("anomaly");
        return llm.completeWithSystemPrompt(systemPrompt,
            "Detect anomalies with baseline: " + baseline, logData);
    }
    
    // Note: We could refactor to a single method that infers persona from user query
    // public String analyze(String userQuery, String cloudProvider) {
    //     String persona = llm.inferPersona(userQuery);
    //     String systemPrompt = PERSONA_SYSTEM_PROMPTS.get(persona);
    //     // ... fetch appropriate data based on persona
    //     return llm.completeWithSystemPrompt(systemPrompt, userQuery, data);
    // }
    
    private String formatLogs(List<LogEntry> logs) {
        return logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
    }
    
    public Map<String, SourceCapabilities> discoverCapabilities() {
        return sources.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getCapabilities()
            ));
    }
}