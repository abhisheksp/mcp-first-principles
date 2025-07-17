package com.watchtower;

import com.watchtower.functions.*;
import com.watchtower.llm.*;
import com.watchtower.protocol.CloudLogSourceProtocolClient;
import com.watchtower.protocol.CloudLogSourceProtocolClientImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Multi-source protocol agent demonstrating the MCP vision
 * 
 * THE MCP VISION: One agent, multiple protocol sources, LLM orchestration across all!
 */
@Slf4j
public class WatchTowerAgent {
    private final Map<String, CloudLogSourceProtocolClient> protocolClients;
    private final LLMFake llm = new LLMFake();
    
    /**
     * Create agent connected to multiple protocol servers - THE MCP VISION!
     */
    public WatchTowerAgent(Map<String, ServerConfig> serverConfigs) {
        this.protocolClients = initializeProtocolClients(serverConfigs);
        
        log.info("WatchTower.AI initialized with {} protocol sources: {}", 
                protocolClients.size(), protocolClients.keySet());
        
        // Discover and log all available functions
        var allFunctions = discoverAllFunctions();
        log.info("Available functions: {}", 
                allFunctions.stream().map(FunctionDefinition::getName).collect(Collectors.toList()));
    }
    
    /**
     * Single entry point for analysis - LLM orchestrates across ALL connected sources
     */
    public String analyze(String userQuery) {
        log.info("Starting multi-source analysis for query: {}", userQuery);
        
        // Build conversation with all available functions
        var conversation = new ArrayList<Map<String, Object>>();
        conversation.add(Map.of(
            "role", "user",
            "content", userQuery + " (multi-source analysis available)"
        ));
        
        var allFunctions = discoverAllFunctions();
        llm.reset();
        
        // Agent-LLM loop with function calling
        return executeConversationLoop(conversation, allFunctions);
    }
    
    /**
     * Close all protocol connections
     */
    public void close() {
        protocolClients.values().forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing protocol client: {}", e.getMessage());
            }
        });
    }
    
    // --- Private Implementation ---
    
    private Map<String, CloudLogSourceProtocolClient> initializeProtocolClients(Map<String, ServerConfig> serverConfigs) {
        return serverConfigs.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> connectToServer(entry.getKey(), entry.getValue())
            ));
    }
    
    private CloudLogSourceProtocolClient connectToServer(String sourceName, ServerConfig config) {
        try {
            log.info("Connecting to {} at {}:{}", sourceName, config.getHost(), config.getPort());
            var client = new CloudLogSourceProtocolClientImpl(config.getHost(), config.getPort());
            client.initialize(config.getCredentials());
            log.info("Successfully connected to {}", sourceName);
            return client;
        } catch (IOException e) {
            var message = String.format("Failed to connect to %s: %s", sourceName, e.getMessage());
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }
    
    private List<FunctionDefinition> discoverAllFunctions() {
        return protocolClients.entrySet().stream()
            .flatMap(entry -> discoverFunctionsFromSource(entry.getKey(), entry.getValue()).stream())
            .collect(Collectors.toList());
    }
    
    private List<FunctionDefinition> discoverFunctionsFromSource(String sourceName, CloudLogSourceProtocolClient client) {
        try {
            var sourceFunctions = client.discover();
            log.info("Discovered {} functions from {}", sourceFunctions.size(), sourceName);
            
            // Namespace functions with source name (AWS.fetchLogs, GCP.fetchLogs)
            return sourceFunctions.stream()
                .map(f -> FunctionDefinition.builder()
                    .name(sourceName + "." + f.getName())
                    .description(f.getDescription() + " (from " + sourceName + ")")
                    .parameters(f.getParameters())
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to discover functions from {}: {}", sourceName, e.getMessage());
            return List.of();
        }
    }
    
    private String executeConversationLoop(List<Map<String, Object>> conversation, List<FunctionDefinition> allFunctions) {
        for (int iteration = 0; iteration < 10; iteration++) { // Safety limit
            var response = llm.completeWithFunctions(conversation, allFunctions);
            log.info("LLM response: {}", response.getContent());
            
            if (response.getFunctionCall() == null) {
                return response.getContent(); // Final answer
            }
            
            // Execute function call and continue conversation
            var functionCall = response.getFunctionCall();
            log.info("Executing function: {} with args: {}", functionCall.getName(), functionCall.getArguments());
            
            var result = executeProtocolFunction(functionCall);
            
            // Add to conversation
            conversation.add(Map.of(
                "role", "assistant",
                "content", response.getContent(),
                "function_call", functionCall
            ));
            
            conversation.add(Map.of(
                "role", "function",
                "name", functionCall.getName(),
                "content", formatFunctionResult(result)
            ));
        }
        
        return "Analysis incomplete - reached iteration limit";
    }
    
    private FunctionResult executeProtocolFunction(FunctionCall call) {
        var functionName = call.getName();
        var parts = functionName.split("\\.", 2);
        
        if (parts.length != 2) {
            return FunctionResult.error(functionName, "Invalid function format. Expected: SOURCE.function");
        }
        
        var sourceName = parts[0];
        var actualFunction = parts[1];
        var client = protocolClients.get(sourceName);
        
        if (client == null) {
            return FunctionResult.error(functionName, "Unknown source: " + sourceName);
        }
        
        try {
            var result = client.execute(actualFunction, call.getArguments());
            return FunctionResult.success(functionName, result);
        } catch (Exception e) {
            return FunctionResult.error(functionName, "Execution failed: " + e.getMessage());
        }
    }
    
    private String formatFunctionResult(FunctionResult result) {
        if (!result.isSuccess()) {
            return "Error: " + result.getError();
        }
        
        if (result.getResult() instanceof List<?> list && !list.isEmpty()) {
            var first = list.get(0);
            if (first instanceof com.watchtower.model.LogEntry) {
                return formatLogs((List<com.watchtower.model.LogEntry>) list);
            } else if (first instanceof com.watchtower.model.Metric) {
                return formatMetrics((List<com.watchtower.model.Metric>) list);
            } else {
                return result.getResult().toString();
            }
        }
        
        return result.getResult().toString();
    }
    
    private String formatLogs(List<com.watchtower.model.LogEntry> logs) {
        return String.format("Found %d log entries:\n%s", 
            logs.size(),
            logs.stream()
                .limit(10)
                .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
                .collect(Collectors.joining("\n"))
        );
    }
    
    private String formatMetrics(List<com.watchtower.model.Metric> metrics) {
        return String.format("Found %d metric data points:\n%s",
            metrics.size(), 
            metrics.stream()
                .limit(10)
                .map(m -> String.format("[%s] %s: %.2f %s", 
                    m.getTimestamp(), m.getName(), m.getValue(), m.getUnit()))
                .collect(Collectors.joining("\n"))
        );
    }
}