package com.watchtower;

import com.watchtower.functions.*;
import com.watchtower.llm.*;
import com.watchtower.model.*;
import com.watchtower.protocol.CloudLogSourceProtocolClient;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Protocol-based multi-source agent (Phase 6)
 * This demonstrates the MCP vision: one agent, multiple protocol sources!
 */
@Slf4j
public class WatchTowerAgent {
    private final Map<String, CloudLogSourceProtocolClient> protocolClients; // Multi-source protocol support
    private final LLMFake llm;
    
    /**
     * Configuration for connecting to a protocol server
     */
    public static class ServerConfig {
        public final String host;
        public final int port;
        public final Map<String, String> credentials;
        
        public ServerConfig(String host, int port, Map<String, String> credentials) {
            this.host = host;
            this.port = port;
            this.credentials = credentials;
        }
    }
    
    /**
     * Multi-source constructor - THE MCP VISION!
     * Connect to multiple protocol servers simultaneously.
     * The LLM can discover and use functions from ALL sources!
     */
    public WatchTowerAgent(Map<String, ServerConfig> serverConfigs) {
        this.protocolClients = initializeProtocolClients(serverConfigs);
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized with MULTI-SOURCE protocol support");
        System.out.println(">>> Connected to " + protocolClients.size() + " sources: " + 
            protocolClients.keySet());
        
        // Discover functions from ALL sources
        List<FunctionDefinition> allFunctions = discoverAllFunctions();
        System.out.println(">>> Available functions from all sources: " + 
            allFunctions.stream().map(f -> f.getName()).collect(Collectors.toList()));
    }
    
    /**
     * Single-source constructor for backward compatibility.
     * Creates a single-server configuration from a protocol client.
     */
    public WatchTowerAgent(CloudLogSourceProtocolClient client, String sourceName) {
        Map<String, CloudLogSourceProtocolClient> clients = new HashMap<>();
        clients.put(sourceName, client);
        this.protocolClients = clients;
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized with single protocol source: " + sourceName);
        
        // Discover functions from the source
        List<FunctionDefinition> allFunctions = discoverAllFunctions();
        System.out.println(">>> Available functions: " + 
            allFunctions.stream().map(f -> f.getName()).collect(Collectors.toList()));
    }
    
    /**
     * Initialize protocol clients for multi-source access
     */
    private Map<String, CloudLogSourceProtocolClient> initializeProtocolClients(Map<String, ServerConfig> serverConfigs) {
        Map<String, CloudLogSourceProtocolClient> clients = new HashMap<>();
        
        for (Map.Entry<String, ServerConfig> entry : serverConfigs.entrySet()) {
            String sourceName = entry.getKey();
            ServerConfig config = entry.getValue();
            
            try {
                System.out.println(">>> Connecting to " + sourceName + " at " + config.host + ":" + config.port);
                CloudLogSourceProtocolClient client = new CloudLogSourceProtocolClient(config.host, config.port);
                client.initialize(config.credentials);
                clients.put(sourceName, client);
                System.out.println(">>> Successfully connected to " + sourceName);
            } catch (IOException e) {
                System.err.println(">>> Failed to connect to " + sourceName + ": " + e.getMessage());
                throw new RuntimeException("Failed to connect to " + sourceName, e);
            }
        }
        
        return clients;
    }
    
    /**
     * Discover functions from all connected protocol sources
     */
    private List<FunctionDefinition> discoverAllFunctions() {
        if (protocolClients == null || protocolClients.isEmpty()) {
            return new ArrayList<>(); // No sources available
        }
        
        List<FunctionDefinition> allFunctions = new ArrayList<>();
        
        for (Map.Entry<String, CloudLogSourceProtocolClient> entry : protocolClients.entrySet()) {
            String sourceName = entry.getKey();
            CloudLogSourceProtocolClient client = entry.getValue();
            
            try {
                List<FunctionDefinition> sourceFunctions = client.discover();
                
                // Namespace the functions with source name (e.g., AWS.fetchLogs, GCP.fetchLogs)
                List<FunctionDefinition> namespacedFunctions = sourceFunctions.stream()
                    .map(f -> FunctionDefinition.builder()
                        .name(sourceName + "." + f.getName())
                        .description(f.getDescription() + " (from " + sourceName + ")")
                        .parameters(f.getParameters())
                        .build())
                    .collect(Collectors.toList());
                
                allFunctions.addAll(namespacedFunctions);
                System.out.println(">>> Discovered " + sourceFunctions.size() + " functions from " + sourceName);
            } catch (Exception e) {
                System.err.println(">>> Failed to discover functions from " + sourceName + ": " + e.getMessage());
            }
        }
        
        return allFunctions;
    }
    
    /**
     * Analyze using function calling - LLM orchestrates the investigation across all sources
     */
    public String analyze(String userQuery, String cloudProvider) {
        // Always use multi-source protocol mode
        return analyzeMultiSource(userQuery);
    }
    
    /**
     * Simplified methods for backward compatibility
     */
    public String troubleshootErrors(String query) {
        return analyze(query);
    }
    
    public String analyzeWithFunctions(String query) {
        return analyze(query);
    }
    
    /**
     * Multi-source analyze method - THE MCP VISION IN ACTION!
     * The LLM can call functions from ANY connected source
     */
    private String analyzeMultiSource(String userQuery) {
        // Initialize conversation
        List<Map<String, Object>> conversation = new ArrayList<>();
        conversation.add(Map.of(
            "role", "user",
            "content", userQuery + " (multi-source analysis available)"
        ));
        
        // Get all available functions from all sources
        List<FunctionDefinition> allFunctions = discoverAllFunctions();
        
        // Agent-LLM loop with multi-source function calling
        llm.reset();
        int iterations = 0;
        
        while (iterations < 10) { // Safety limit
            iterations++;
            
            // Get LLM response with ALL available functions
            LLMResponse response = llm.completeWithFunctions(conversation, allFunctions);
            
            System.out.println(">>> LLM: " + response.getContent());
            
            if (response.getFunctionCall() != null) {
                // LLM wants to call a function
                FunctionCall call = response.getFunctionCall();
                System.out.println(">>> Multi-source function request: " + call.getName() + 
                                 " with args: " + call.getArguments());
                
                // Execute the function across protocol clients
                FunctionResult result = executeProtocolFunction(call);
                
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
        
        return "Multi-source analysis incomplete - reached iteration limit";
    }
    
    /**
     * Execute a function across protocol clients (multi-source support)
     */
    private FunctionResult executeProtocolFunction(FunctionCall call) {
        String functionName = call.getName();
        
        // Parse namespace (e.g., "AWS.fetchLogs" -> source="AWS", function="fetchLogs")
        String[] parts = functionName.split("\\.", 2);
        if (parts.length == 2) {
            String sourceName = parts[0];
            String actualFunction = parts[1];
            
            CloudLogSourceProtocolClient client = protocolClients.get(sourceName);
            if (client != null) {
                try {
                    Object result = client.execute(actualFunction, call.getArguments());
                    return FunctionResult.success(functionName, result);
                } catch (Exception e) {
                    return FunctionResult.error(functionName, "Protocol execution failed: " + e.getMessage());
                }
            } else {
                return FunctionResult.error(functionName, "Unknown source: " + sourceName);
            }
        }
        
        return FunctionResult.error(functionName, "Invalid function name format. Expected: SOURCE.function");
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
    
    /**
     * Simple analyze method for multi-source agents
     */
    public String analyze(String userQuery) {
        return analyzeMultiSource(userQuery);
    }
    
    /**
     * Close all protocol connections
     */
    public void close() {
        if (protocolClients != null) {
            for (CloudLogSourceProtocolClient client : protocolClients.values()) {
                try {
                    client.close();
                } catch (Exception e) {
                    System.err.println("Error closing protocol client: " + e.getMessage());
                }
            }
        }
    }
    
    public List<FunctionDefinition> getAvailableFunctions() {
        return discoverAllFunctions();
    }
}