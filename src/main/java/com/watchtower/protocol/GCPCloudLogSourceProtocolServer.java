package com.watchtower.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchtower.sources.GCPLogSource;
import com.watchtower.sources.CloudLogSource;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.watchtower.protocol.ProtocolMessages.*;

/**
 * GCP-specific MCP server implementation.
 * This represents a real-world Google Cloud MCP server that exposes GCP logging capabilities.
 * 
 * In production, this would be a standalone service that Google Cloud teams maintain,
 * exposing their Cloud Logging, Cloud Monitoring, and other tools through the MCP protocol.
 */
public class GCPCloudLogSourceProtocolServer implements CloudLogSourceProtocol {
    private final GCPLogSource gcpSource;
    private final ObjectMapper mapper = new ObjectMapper();
    
    public GCPCloudLogSourceProtocolServer() {
        this.gcpSource = new GCPLogSource();
    }
    
    @Override
    public JsonNode initialize(Request request) {
        try {
            Map<String, String> credentials = new HashMap<>();
            if (request.getParams() != null) {
                InitializeParams params = mapper.convertValue(request.getParams(), InitializeParams.class);
                if (params.getCredentials() != null) {
                    credentials = params.getCredentials();
                }
            }
            
            gcpSource.initialize(credentials);
            
            Map<String, Object> result = new HashMap<>();
            result.put("provider", "GCP");
            result.put("status", "initialized");
            result.put("capabilities", Arrays.asList("Cloud Logging", "Cloud Monitoring", "Error Reporting"));
            return mapper.convertValue(result, JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException("GCP initialization failed", e);
        }
    }
    
    @Override
    public JsonNode discover() {
        List<FunctionInfo> functions = Arrays.asList(
            new FunctionInfo(
                "fetchLogs",
                "Fetch Cloud Logging entries from GCP",
                Arrays.asList(
                    new ParameterInfo("resource", "string", "GCP service name (e.g., gke, cloud-functions)", true),
                    new ParameterInfo("filter", "string", "Log severity filter (INFO, ERROR, etc.)", true),
                    new ParameterInfo("limit", "integer", "Maximum logs to return (default: 100)", true)
                )
            ),
            new FunctionInfo(
                "fetchMetrics",
                "Fetch Cloud Monitoring metrics from GCP",
                Arrays.asList(
                    new ParameterInfo("resource", "string", "GCP service name", true),
                    new ParameterInfo("metricName", "string", "Cloud Monitoring metric name", true),
                    new ParameterInfo("timeRange", "string", "Time range (1h, 6h, 24h)", true)
                )
            )
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("functions", functions);
        result.put("provider", "GCP");
        result.put("version", "1.0");
        return mapper.convertValue(result, JsonNode.class);
    }
    
    @Override
    public JsonNode execute(Request request) {
        ExecuteParams params = mapper.convertValue(request.getParams(), ExecuteParams.class);
        String function = params.getFunction();
        Map<String, Object> args = params.getArguments();
        
        Map<String, Object> result = new HashMap<>();
        
        switch (function) {
            case "fetchLogs":
                List<LogEntry> logs = gcpSource.fetchLogs(
                    (String) args.get("resource"),
                    (String) args.get("filter"),
                    ((Number) args.get("limit")).intValue()
                );
                result.put("logs", logs);
                result.put("source", "GCP Cloud Logging");
                break;
                
            case "fetchMetrics":
                List<Metric> metrics = gcpSource.fetchMetrics(
                    (String) args.get("resource"),
                    (String) args.get("metricName"),
                    (String) args.get("timeRange")
                );
                result.put("metrics", metrics);
                result.put("source", "GCP Cloud Monitoring");
                break;
                
            default:
                throw new IllegalArgumentException("GCP server doesn't support function: " + function);
        }
        
        return mapper.convertValue(result, JsonNode.class);
    }
    
    /**
     * Handle a single client connection with proper protocol implementation
     */
    public void handleClient(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out, true);
        
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                System.out.println(">>> GCP Server received: " + line);
                Request request = mapper.readValue(line, Request.class);
                Response response = handleRequest(request);
                String responseJson = mapper.writeValueAsString(response);
                System.out.println(">>> GCP Server sending: " + responseJson);
                writer.println(responseJson);
            } catch (Exception e) {
                Response errorResponse = new Response();
                errorResponse.setJsonrpc("2.0");
                errorResponse.setError(new ProtocolMessages.Error(-32603, "GCP server error: " + e.getMessage(), null));
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
                    response.setResult(initialize(request));
                    break;
                case "discover":
                    response.setResult(discover());
                    break;
                case "execute":
                    response.setResult(execute(request));
                    break;
                default:
                    response.setError(new ProtocolMessages.Error(-32601, "Method not found: " + request.getMethod(), null));
            }
        } catch (Exception e) {
            response.setError(new ProtocolMessages.Error(-32603, "GCP server error: " + e.getMessage(), null));
        }
        
        return response;
    }
    
    /**
     * Main method to run the GCP MCP server standalone.
     * Usage: java GCPCloudLogSourceProtocolServer [port]
     */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8002;
        
        GCPCloudLogSourceProtocolServer server = new GCPCloudLogSourceProtocolServer();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🔵 GCP MCP Server listening on port " + port);
            System.out.println("📡 Exposing Cloud Logging, Monitoring, and Error Reporting capabilities");
            System.out.println("⏳ Waiting for connections...");
            
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("✅ Client connected from " + client.getInetAddress());
                
                // Handle each client in a new thread
                new Thread(() -> {
                    try {
                        server.handleClient(client.getInputStream(), client.getOutputStream());
                    } catch (IOException e) {
                        System.err.println("❌ Error handling client: " + e.getMessage());
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