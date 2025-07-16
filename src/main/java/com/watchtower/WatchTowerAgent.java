package com.watchtower;

import com.watchtower.sources.*;
import com.watchtower.functions.*;
import com.watchtower.llm.*;
import com.watchtower.model.*;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Clean version with function calling only
 */
@Slf4j
public class WatchTowerAgent {
    public final Map<String, CloudLogSource> sources;
    private final LLMFake llm;
    
    // Available functions that can be called
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
        
        System.out.println(">>> WatchTower.AI initialized");
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
     * Analyze using function calling - LLM orchestrates the investigation
     */
    public String analyze(String userQuery, String cloudProvider) {
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
            
            // Get LLM response
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
                
            } else {
                // LLM has final answer
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
        
        if (result.getResult() instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof LogEntry) {
                return formatLogs((List<LogEntry>) list);
            } else if (first instanceof Metric) {
                return formatMetrics((List<Metric>) list);
            }
        }
        
        return result.getResult().toString();
    }
    
    private String formatLogs(List<LogEntry> logs) {
        return "Found " + logs.size() + " log entries:\n" +
            logs.stream()
                .limit(10)
                .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
                .collect(Collectors.joining("\n"));
    }
    
    private String formatMetrics(List<Metric> metrics) {
        return "Found " + metrics.size() + " metric data points:\n" +
            metrics.stream()
                .limit(10)
                .map(m -> String.format("[%s] %s: %.2f %s", 
                    m.getTimestamp(), m.getName(), m.getValue(), m.getUnit()))
                .collect(Collectors.joining("\n"));
    }
    
    public List<FunctionDefinition> getAvailableFunctions() {
        return availableFunctions;
    }
}