package com.watchtower.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchtower.sources.CloudLogSource;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;
import com.watchtower.functions.FunctionDefinition;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.watchtower.protocol.ProtocolMessages.*;

/**
 * Protocol client implementation that enables function discovery and execution.
 * This is what makes the Agent/LLM loop work over the protocol!
 * 
 * KEY FEATURE: Can be used standalone for multi-source access or
 * as a CloudLogSource adapter for backward compatibility.
 * 
 * Implements both CloudLogSourceProtocolClient (for standardized protocol methods) and 
 * CloudLogSource (for backward compatibility with existing code).
 */
public class CloudLogSourceProtocolClientImpl implements CloudLogSourceProtocolClient, CloudLogSource {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger requestId = new AtomicInteger(0);
    private final Socket socket;
    
    public CloudLogSourceProtocolClientImpl(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }
    
    // Protected constructor for subclassing or testing
    protected CloudLogSourceProtocolClientImpl(Socket socket, BufferedReader reader, PrintWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }
    
    @Override
    public void initialize(Map<String, String> config) {
        InitializeParams params = new InitializeParams(config);
        Request request = new Request("2.0", "initialize", mapper.valueToTree(params), 
                                     String.valueOf(requestId.incrementAndGet()));
        
        Response response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Initialization failed: " + response.getError().getMessage());
        }
    }
    
    @Override
    public List<LogEntry> fetchLogs(String resource, String filter, int limit) {
        Map<String, Object> args = new HashMap<>();
        args.put("resource", resource);
        args.put("filter", filter);
        args.put("limit", limit);
        
        ExecuteParams params = new ExecuteParams("fetchLogs", args);
        Request request = new Request("2.0", "execute", mapper.valueToTree(params),
                                     String.valueOf(requestId.incrementAndGet()));
        
        Response response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("fetchLogs failed: " + response.getError().getMessage());
        }
        
        return mapper.convertValue(
            response.getResult().get("logs"),
            mapper.getTypeFactory().constructCollectionType(List.class, LogEntry.class)
        );
    }
    
    @Override
    public List<Metric> fetchMetrics(String resource, String metricName, String timeRange) {
        Map<String, Object> args = new HashMap<>();
        args.put("resource", resource);
        args.put("metricName", metricName);
        args.put("timeRange", timeRange);
        
        ExecuteParams params = new ExecuteParams("fetchMetrics", args);
        Request request = new Request("2.0", "execute", mapper.valueToTree(params),
                                     String.valueOf(requestId.incrementAndGet()));
        
        Response response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("fetchMetrics failed: " + response.getError().getMessage());
        }
        
        return mapper.convertValue(
            response.getResult().get("metrics"),
            mapper.getTypeFactory().constructCollectionType(List.class, Metric.class)
        );
    }
    
    @Override
    public String getCloudProvider() {
        Request request = new Request("2.0", "discover", null,
                                     String.valueOf(requestId.incrementAndGet()));
        
        Response response = sendRequest(request);
        // In a real implementation, we'd parse the provider from discovery
        return "Protocol-based Source";
    }
    
    /**
     * Discover available functions from the protocol server.
     * This is KEY for the LLM to know what it can call!
     */
    public List<FunctionDefinition> discover() {
        Request request = new Request("2.0", "discover", null,
                                     String.valueOf(requestId.incrementAndGet()));
        
        Response response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Discovery failed: " + response.getError().getMessage());
        }
        
        // Convert protocol format to FunctionDefinition format
        DiscoverResult discoverResult = mapper.convertValue(
            response.getResult(), DiscoverResult.class
        );
        
        return discoverResult.getFunctions().stream()
            .map(f -> FunctionDefinition.builder()
                .name(f.getName())
                .description(f.getDescription())
                .parameters(f.getParameters().stream()
                    .map(p -> new FunctionDefinition.Parameter(p.getName(), p.getType(), p.getDescription()))
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * Execute a function on the protocol server.
     * This is how the LLM's function calls get executed!
     */
    public Object execute(String functionName, Map<String, Object> parameters) {
        ExecuteParams params = new ExecuteParams(functionName, parameters);
        Request request = new Request("2.0", "execute", mapper.convertValue(params, com.fasterxml.jackson.databind.JsonNode.class),
                                     String.valueOf(requestId.incrementAndGet()));
        
        Response response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Execute failed: " + response.getError().getMessage());
        }
        
        // Return the raw result - let the agent format it
        return response.getResult();
    }
    
    private Response sendRequest(Request request) {
        try {
            String requestJson = mapper.writeValueAsString(request);
            System.out.println(">>> Client sending: " + requestJson);
            writer.println(requestJson);
            
            String responseJson = reader.readLine();
            if (responseJson == null) {
                throw new IOException("Connection closed");
            }
            
            System.out.println(">>> Client received: " + responseJson);
            return mapper.readValue(responseJson, Response.class);
        } catch (Exception e) {
            throw new RuntimeException("Protocol communication failed", e);
        }
    }
    
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}