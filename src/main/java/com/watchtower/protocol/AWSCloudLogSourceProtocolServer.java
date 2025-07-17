package com.watchtower.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchtower.sources.AWSLogSource;
import com.watchtower.sources.CloudLogSource;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.watchtower.protocol.ProtocolMessages.*;

/**
 * AWS-specific MCP server implementation.
 * This represents a real-world AWS MCP server that exposes AWS CloudWatch capabilities.
 * 
 * In production, this would be a standalone service that AWS teams maintain,
 * exposing their logging/monitoring tools through the MCP protocol.
 */
public class AWSCloudLogSourceProtocolServer implements CloudLogSourceProtocol {
    private final AWSLogSource awsSource;
    private final ObjectMapper mapper = new ObjectMapper();
    
    public AWSCloudLogSourceProtocolServer() {
        this.awsSource = new AWSLogSource();
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
            
            awsSource.initialize(credentials);
            
            Map<String, Object> result = new HashMap<>();
            result.put("provider", "AWS");
            result.put("status", "initialized");
            result.put("capabilities", Arrays.asList("CloudWatch", "X-Ray", "CloudTrail"));
            return mapper.convertValue(result, JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException("AWS initialization failed", e);
        }
    }
    
    @Override
    public JsonNode discover() {
        List<FunctionInfo> functions = Arrays.asList(
            new FunctionInfo(
                "fetchLogs",
                "Fetch CloudWatch logs from AWS",
                Arrays.asList(
                    new ParameterInfo("resource", "string", "AWS service name (e.g., api-gateway, lambda)", true),
                    new ParameterInfo("filter", "string", "Log level filter (INFO, ERROR, etc.)", true),
                    new ParameterInfo("limit", "integer", "Maximum logs to return (default: 100)", true)
                )
            ),
            new FunctionInfo(
                "fetchMetrics",
                "Fetch CloudWatch metrics from AWS",
                Arrays.asList(
                    new ParameterInfo("resource", "string", "AWS service name", true),
                    new ParameterInfo("metricName", "string", "CloudWatch metric name", true),
                    new ParameterInfo("timeRange", "string", "Time range (1h, 6h, 24h)", true)
                )
            )
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("functions", functions);
        result.put("provider", "AWS");
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
                List<LogEntry> logs = awsSource.fetchLogs(
                    (String) args.get("resource"),
                    (String) args.get("filter"),
                    ((Number) args.get("limit")).intValue()
                );
                result.put("logs", logs);
                result.put("source", "AWS CloudWatch");
                break;
                
            case "fetchMetrics":
                List<Metric> metrics = awsSource.fetchMetrics(
                    (String) args.get("resource"),
                    (String) args.get("metricName"),
                    (String) args.get("timeRange")
                );
                result.put("metrics", metrics);
                result.put("source", "AWS CloudWatch");
                break;
                
            default:
                throw new IllegalArgumentException("AWS server doesn't support function: " + function);
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
                System.out.println(">>> AWS Server received: " + line);
                Request request = mapper.readValue(line, Request.class);
                Response response = handleRequest(request);
                String responseJson = mapper.writeValueAsString(response);
                System.out.println(">>> AWS Server sending: " + responseJson);
                writer.println(responseJson);
            } catch (Exception e) {
                Response errorResponse = new Response();
                errorResponse.setJsonrpc("2.0");
                errorResponse.setError(new ProtocolMessages.Error(-32603, "AWS server error: " + e.getMessage(), null));
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
            response.setError(new ProtocolMessages.Error(-32603, "AWS server error: " + e.getMessage(), null));
        }
        
        return response;
    }
    
    /**
     * Main method to run the AWS MCP server standalone.
     * Usage: java AWSCloudLogSourceProtocolServer [port]
     */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8001;
        
        AWSCloudLogSourceProtocolServer server = new AWSCloudLogSourceProtocolServer();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🔶 AWS MCP Server listening on port " + port);
            System.out.println("📡 Exposing CloudWatch, X-Ray, and CloudTrail capabilities");
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