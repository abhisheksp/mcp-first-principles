package com.watchtower;

import com.watchtower.sources.*;
import com.watchtower.functions.*;
import com.watchtower.llm.*;
import com.watchtower.model.*;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Now with Function Calling!
 * 
 * The LLM can now orchestrate data fetching instead of us hardcoding it.
 */
@Slf4j
public class WatchTowerAgent {
    public final Map<String, CloudLogSource> sources;
    private final LLMFake llm;
    
    // Define available functions
    private final List<FunctionDefinition> availableFunctions = List.of(
        FunctionDefinition.builder()
            .name("fetchLogs")
            .description("Fetch logs from cloud source")
            .parameters(List.of(
                new FunctionDefinition.Parameter("resource", "string", "Service name (e.g., payment-service)"),
                new FunctionDefinition.Parameter("filter", "string", "Log level: ERROR, WARN, INFO"),
                new FunctionDefinition.Parameter("limit", "integer", "Maximum number of logs")
            ))
            .build(),
            
        FunctionDefinition.builder()
            .name("fetchMetrics")
            .description("Fetch metrics from cloud source")
            .parameters(List.of(
                new FunctionDefinition.Parameter("resource", "string", "Service name"),
                new FunctionDefinition.Parameter("metricName", "string", "Metric name: error_rate, cpu_usage, request_count"),
                new FunctionDefinition.Parameter("timeRange", "string", "Time range: 1h, 24h, 7d")
            ))
            .build()
    );
    
    public WatchTowerAgent() {
        this.sources = initializeCloudSources();
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized with function calling");
        System.out.println(">>> Available functions: " + 
            availableFunctions.stream().map(FunctionDefinition::getName).toList());
    }
    
    private Map<String, CloudLogSource> initializeCloudSources() {
        CloudLogSource awsSource = new AWSLogSource();
        awsSource.initialize(Map.of(
            "accessKeyId", System.getenv("AWS_ACCESS_KEY_ID") != null ? System.getenv("AWS_ACCESS_KEY_ID") : "fake-key",
            "secretAccessKey", System.getenv("AWS_SECRET_ACCESS_KEY") != null ? System.getenv("AWS_SECRET_ACCESS_KEY") : "fake-secret",
            "region", "us-east-1"
        ));
        
        CloudLogSource gcpSource = new GCPLogSource();
        gcpSource.initialize(Map.of(
            "serviceAccountPath", System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null ? System.getenv("GOOGLE_APPLICATION_CREDENTIALS") : "fake-path"
        ));
        
        return Map.of("AWS", awsSource, "GCP", gcpSource);
    }
    
    /**
     * NEW: Analyze using function calling - LLM orchestrates!
     */
    public String analyzeWithFunctions(String userQuery, String cloudProvider) {
        System.out.println("\n>>> Starting function-based analysis");
        System.out.println(">>> User query: " + userQuery);
        
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "Unknown cloud provider: " + cloudProvider;
        }
        
        // Initialize conversation
        List<Map<String, Object>> conversation = new ArrayList<>();
        conversation.add(Map.of(
            "role", "user",
            "content", userQuery + " (analyzing " + cloudProvider + ")"
        ));
        
        // Agent-LLM loop
        llm.reset();
        int iterations = 0;
        
        while (iterations < 10) { // Safety limit
            iterations++;
            
            // Get LLM response with function calling
            LLMResponse response = llm.completeWithFunctions(conversation, availableFunctions);
            
            System.out.println(">>> LLM: " + response.getContent());
            
            if (response.getFunctionCall() != null) {
                // LLM wants to call a function
                FunctionCall call = response.getFunctionCall();
                System.out.println(">>> Function request: " + call.getName() + 
                                 " with args: " + call.getArguments());
                
                // Execute the function
                FunctionResult result = executeFunction(call, source);
                
                // Add to conversation
                conversation.add(Map.of(
                    "role", "assistant",
                    "content", response.getContent(),
                    "function_call", call
                ));
                
                conversation.add(Map.of(
                    "role", "function",
                    "name", call.getName(),
                    "content", formatFunctionResult(result)
                ));
                
                System.out.println(">>> Function result provided to LLM");
                
            } else {
                // LLM has final answer
                System.out.println(">>> Analysis complete");
                return response.getContent();
            }
        }
        
        return "Analysis incomplete - reached iteration limit";
    }
    
    /**
     * Execute a function requested by the LLM
     */
    private FunctionResult executeFunction(FunctionCall call, CloudLogSource source) {
        try {
            return switch (call.getName()) {
                case "fetchLogs" -> {
                    String resource = (String) call.getArguments().get("resource");
                    String filter = (String) call.getArguments().get("filter");
                    Integer limit = (Integer) call.getArguments().get("limit");
                    
                    List<LogEntry> logs = source.fetchLogs(resource, filter, limit);
                    yield FunctionResult.success("fetchLogs", logs);
                }
                
                case "fetchMetrics" -> {
                    String resource = (String) call.getArguments().get("resource");
                    String metricName = (String) call.getArguments().get("metricName");
                    String timeRange = (String) call.getArguments().get("timeRange");
                    
                    List<Metric> metrics = source.fetchMetrics(resource, metricName, timeRange);
                    yield FunctionResult.success("fetchMetrics", metrics);
                }
                
                default -> FunctionResult.error(call.getName(), "Unknown function");
            };
        } catch (Exception e) {
            return FunctionResult.error(call.getName(), e.getMessage());
        }
    }
    
    private String formatFunctionResult(FunctionResult result) {
        if (!result.isSuccess()) {
            return "Error: " + result.getError();
        }
        
        if (result.getResult() instanceof List<?> list) {
            if (!list.isEmpty() && list.get(0) instanceof LogEntry) {
                List<LogEntry> logs = (List<LogEntry>) list;
                return "Found " + logs.size() + " log entries:\n" +
                    logs.stream()
                        .limit(10)
                        .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
                        .collect(Collectors.joining("\n"));
            } else if (!list.isEmpty() && list.get(0) instanceof Metric) {
                List<Metric> metrics = (List<Metric>) list;
                return "Found " + metrics.size() + " metric data points:\n" +
                    metrics.stream()
                        .limit(10)
                        .map(m -> String.format("[%s] %s: %.2f %s", 
                            m.getTimestamp(), m.getName(), m.getValue(), m.getUnit()))
                        .collect(Collectors.joining("\n"));
            }
        }
        
        return result.getResult().toString();
    }
    
    // Keep old methods for comparison
    public String troubleshootErrors(String userQuery, String cloudProvider) {
        log.info(">>> OLD METHOD: Troubleshooting with hardcoded sequence");
        
        CloudLogSource source = sources.get(cloudProvider);
        if (source == null) {
            return "Unknown provider: " + cloudProvider;
        }
        
        // WE decide to fetch logs first
        List<LogEntry> logs = source.fetchLogs("payment-service", "ERROR", 1000);
        
        // WE decide to also check metrics
        List<Metric> metrics = source.fetchMetrics("payment-service", "error_rate", "1h");
        
        // WE manually combine the data
        String logData = logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
            
        String metricData = metrics.stream()
            .map(m -> String.format("[%s] %s: %.2f %s", 
                m.getTimestamp(), m.getName(), m.getValue(), m.getUnit()))
            .collect(Collectors.joining("\n"));
        
        String combinedContext = String.format(
            "User Query: %s\n\nError Logs:\n%s\n\nError Rate Metrics:\n%s",
            userQuery, logData, metricData
        );
        
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