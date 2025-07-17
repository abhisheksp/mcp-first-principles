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
    private final Map<String, List<FunctionDefinition>> providerFunctions;
    private final LLMFake llm = new LLMFake();
    
    /**
     * Create agent connected to multiple protocol servers - THE MCP VISION!
     */
    public WatchTowerAgent(Map<String, ServerConfig> serverConfigs) {
        this.protocolClients = initializeProtocolClients(serverConfigs);
        this.providerFunctions = discoverAllProviderFunctions();
        
        log.info("WatchTower.AI initialized with {} protocol sources: {}", 
                protocolClients.size(), protocolClients.keySet());
        
        // Log available functions by provider (like real MCP)
        providerFunctions.forEach((provider, functions) -> {
            log.info("Provider {}: {} functions available: {}", 
                    provider, functions.size(), 
                    functions.stream().map(FunctionDefinition::getName).collect(Collectors.toList()));
        });
    }
    
    /**
     * Single entry point for analysis - LLM orchestrates across ALL connected sources
     */
    public String analyze(String userQuery) {
        log.info("Starting multi-source analysis for query: {}", userQuery);
        
        // Build conversation with provider-aware context
        var conversation = new ArrayList<Map<String, Object>>();
        
        // Add provider context to the user query
        var providerContext = buildProviderContext();
        conversation.add(Map.of(
            "role", "user",
            "content", userQuery + "\n\nAvailable providers and their functions:\n" + providerContext
        ));
        
        llm.reset();
        
        // Agent-LLM loop with provider-aware function calling
        return executeConversationLoop(conversation, providerFunctions);
    }
    
    /**
     * Get available functions by provider (useful for debugging and testing)
     */
    public Map<String, List<FunctionDefinition>> getProviderFunctions() {
        return Collections.unmodifiableMap(providerFunctions);
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
    
    /**
     * Discover functions from all providers, maintaining provider separation (like real MCP)
     */
    private Map<String, List<FunctionDefinition>> discoverAllProviderFunctions() {
        return protocolClients.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> discoverFunctionsFromSource(entry.getKey(), entry.getValue())
            ));
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
    
    /**
     * Build provider context string for LLM to understand available providers
     */
    private String buildProviderContext() {
        return providerFunctions.entrySet().stream()
            .map(entry -> {
                var provider = entry.getKey();
                var functions = entry.getValue();
                var functionNames = functions.stream()
                    .map(f -> "  - " + f.getName() + ": " + f.getDescription())
                    .collect(Collectors.joining("\n"));
                return "Provider: " + provider + "\n" + functionNames;
            })
            .collect(Collectors.joining("\n\n"));
    }
    
    
    private String executeConversationLoop(List<Map<String, Object>> conversation, Map<String, List<FunctionDefinition>> providerFunctions) {
        for (int iteration = 0; iteration < 10; iteration++) { // Safety limit
            // Convert provider functions to flat list for LLM function calling
            var allFunctions = providerFunctions.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
            
            var response = llm.completeWithFunctions(conversation, allFunctions);
            log.info("LLM response: {}", response.getContent());
            
            if (response.getFunctionCall() == null) {
                return response.getContent(); // Final answer
            }
            
            // Execute function call and continue conversation
            var functionCall = response.getFunctionCall();
            log.info("Executing function: {} with args: {}", functionCall.getName(), functionCall.getArguments());
            
            var result = executeProtocolFunction(functionCall, providerFunctions);
            
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
    
    private FunctionResult executeProtocolFunction(FunctionCall call, Map<String, List<FunctionDefinition>> providerFunctions) {
        var functionName = call.getName();
        var parts = functionName.split("\\.", 2);
        
        if (parts.length != 2) {
            return FunctionResult.error(functionName, "Invalid function format. Expected: PROVIDER.function");
        }
        
        var providerName = parts[0];
        var actualFunction = parts[1];
        
        // Verify the function exists in the provider's function list
        var providerFunctionList = providerFunctions.get(providerName);
        if (providerFunctionList == null) {
            return FunctionResult.error(functionName, "Unknown provider: " + providerName);
        }
        
        // Verify the specific function exists for this provider
        var functionExists = providerFunctionList.stream()
            .anyMatch(f -> f.getName().equals(functionName));
        
        if (!functionExists) {
            return FunctionResult.error(functionName, "Function not found in provider " + providerName);
        }
        
        // Get the client for this provider
        var client = protocolClients.get(providerName);
        if (client == null) {
            return FunctionResult.error(functionName, "No client available for provider: " + providerName);
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