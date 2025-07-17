package com.watchtower.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.watchtower.sources.CloudLogSource;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;
import com.watchtower.sources.AWSLogSource;
import com.watchtower.sources.GCPLogSource;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.watchtower.protocol.ProtocolMessages.*;

/**
 * Protocol server that wraps any CloudLogSource and exposes it via our standard protocol.
 * This demonstrates how a single protocol can work with different implementations.
 */
public class CloudLogSourceProtocolServer {
    private final CloudLogSource source;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String provider;
    
    public CloudLogSourceProtocolServer(String provider) {
        this.provider = provider;
        this.source = createSource(provider);
    }
    
    private CloudLogSource createSource(String provider) {
        switch (provider.toUpperCase()) {
            case "AWS":
                return new AWSLogSource();
            case "GCP":
                return new GCPLogSource();
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }
    
    public void handleClient(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out, true);
        
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                System.out.println(">>> Server received: " + line);
                Request request = mapper.readValue(line, Request.class);
                Response response = handleRequest(request);
                String responseJson = mapper.writeValueAsString(response);
                System.out.println(">>> Server sending: " + responseJson);
                writer.println(responseJson);
            } catch (Exception e) {
                Response errorResponse = new Response();
                errorResponse.setJsonrpc("2.0");
                errorResponse.setError(new ProtocolMessages.Error(-32603, "Internal error: " + e.getMessage(), null));
                writer.println(mapper.writeValueAsString(errorResponse));
            }
        }
    }
    
    private Response handleRequest(Request request) {
        Response response = new Response();
        response.setJsonrpc("2.0");
        response.setId(request.getId());
        
        try {
            switch (request.getMethod()) {
                case "initialize":
                    response.setResult(handleInitialize(request));
                    break;
                case "discover":
                    response.setResult(handleDiscover());
                    break;
                case "execute":
                    response.setResult(handleExecute(request));
                    break;
                default:
                    response.setError(new ProtocolMessages.Error(-32601, "Method not found: " + request.getMethod(), null));
            }
        } catch (Exception e) {
            response.setError(new ProtocolMessages.Error(-32603, "Internal error: " + e.getMessage(), null));
        }
        
        return response;
    }
    
    private JsonNode handleInitialize(Request request) {
        try {
            Map<String, String> credentials = new HashMap<>();
            if (request.getParams() != null) {
                InitializeParams params = mapper.convertValue(request.getParams(), InitializeParams.class);
                if (params.getCredentials() != null) {
                    credentials = params.getCredentials();
                }
            }
            source.initialize(credentials);
            
            // Create result safely using Map and converting to JsonNode
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("provider", provider);
            resultMap.put("status", "initialized");
            return mapper.convertValue(resultMap, JsonNode.class);
        } catch (Exception e) {
            System.err.println("Error in handleInitialize: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize", e);
        }
    }
    
    private JsonNode handleDiscover() {
        List<FunctionInfo> functions = Arrays.asList(
            new FunctionInfo(
                "fetchLogs",
                "Fetch logs from cloud source",
                Arrays.asList(
                    new ParameterInfo("resource", "string", "Service name", true),
                    new ParameterInfo("filter", "string", "Log level filter", true),
                    new ParameterInfo("limit", "integer", "Maximum logs to return", true)
                )
            ),
            new FunctionInfo(
                "fetchMetrics",
                "Fetch metrics from cloud source",
                Arrays.asList(
                    new ParameterInfo("resource", "string", "Service name", true),
                    new ParameterInfo("metricName", "string", "Metric name", true),
                    new ParameterInfo("timeRange", "string", "Time range (e.g., 1h, 24h)", true)
                )
            )
        );
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("functions", functions);
        return mapper.convertValue(resultMap, JsonNode.class);
    }
    
    private JsonNode handleExecute(Request request) {
        ExecuteParams params = mapper.convertValue(request.getParams(), ExecuteParams.class);
        String function = params.getFunction();
        Map<String, Object> args = params.getArguments();
        
        Map<String, Object> result = new HashMap<>();
        
        switch (function) {
            case "fetchLogs":
                List<LogEntry> logs = source.fetchLogs(
                    (String) args.get("resource"),
                    (String) args.get("filter"),
                    ((Number) args.get("limit")).intValue()
                );
                result.put("logs", logs);
                break;
                
            case "fetchMetrics":
                List<Metric> metrics = source.fetchMetrics(
                    (String) args.get("resource"),
                    (String) args.get("metricName"),
                    (String) args.get("timeRange")
                );
                result.put("metrics", metrics);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown function: " + function);
        }
        
        return mapper.convertValue(result, JsonNode.class);
    }
    
    /**
     * Main method to run the server standalone.
     * Usage: java CloudLogSourceProtocolServer <provider> <port>
     * Example: java CloudLogSourceProtocolServer AWS 8001
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: CloudLogSourceProtocolServer <provider> <port>");
            System.exit(1);
        }
        
        String provider = args[0];
        int port = Integer.parseInt(args[1]);
        
        CloudLogSourceProtocolServer server = new CloudLogSourceProtocolServer(provider);
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(provider + " Protocol Server listening on port " + port);
            System.out.println("Waiting for connections...");
            
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connected from " + client.getInetAddress());
                
                // Handle each client in a new thread
                new Thread(() -> {
                    try {
                        server.handleClient(client.getInputStream(), client.getOutputStream());
                    } catch (IOException e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }).start();
            }
        }
    }
}