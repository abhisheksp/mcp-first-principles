package com.watchtower.transports;

import com.watchtower.sources.AWSLogSource;
import com.watchtower.functions.*;
import com.watchtower.model.*;
import com.watchtower.WatchTowerAgent;
import java.util.*;

/**
 * REST API wrapper for AWS CloudLogSource
 * 
 * Exposes CloudLogSource functions via REST endpoints
 */
public class AWSCloudLogSourceREST {
    private final AWSLogSource source;
    private final WatchTowerAgent agent; // For function execution
    
    public AWSCloudLogSourceREST() {
        this.source = new AWSLogSource();
        this.source.initialize(Map.of(
            "accessKeyId", System.getenv("AWS_ACCESS_KEY_ID") != null ? System.getenv("AWS_ACCESS_KEY_ID") : "fake-key",
            "secretAccessKey", System.getenv("AWS_SECRET_ACCESS_KEY") != null ? System.getenv("AWS_SECRET_ACCESS_KEY") : "fake-secret",
            "region", "us-east-1"
        ));
        this.agent = new WatchTowerAgent();
    }
    
    /**
     * POST /api/v1/aws/functions/execute
     * 
     * Request body:
     * {
     *   "function": "fetchLogs",
     *   "arguments": {
     *     "resource": "payment-service",
     *     "filter": "ERROR",
     *     "limit": 1000
     *   }
     * }
     */
    public Map<String, Object> executeFunction(Map<String, Object> request) {
        // REST-specific: Parse JSON request body
        String functionName = (String) request.get("function");
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
        
        // REST-specific: Validate required fields
        if (functionName == null || arguments == null) {
            return Map.of(
                "error", Map.of(
                    "code", 400,
                    "message", "Missing required fields: function, arguments"
                )
            );
        }
        
        // REST-specific: Authentication would happen here via headers
        // String authToken = headers.get("Authorization");
        // validateToken(authToken);
        
        try {
            // Create function call
            FunctionCall call = new FunctionCall(functionName, arguments);
            
            // Execute using the underlying source
            // Note: In reality, this would have REST-specific error handling,
            // response formatting, status codes, etc.
            Object result = executeFunctionInternal(call);
            
            // REST-specific: Format response as JSON
            return Map.of(
                "status", "success",
                "function", functionName,
                "result", result
            );
            
        } catch (Exception e) {
            // REST-specific: HTTP error response format
            return Map.of(
                "error", Map.of(
                    "code", 500,
                    "message", e.getMessage()
                )
            );
        }
    }
    
    /**
     * GET /api/v1/aws/functions
     * 
     * Returns OpenAPI-formatted function definitions
     */
    public Map<String, Object> getFunctions() {
        // REST-specific: Convert our FunctionDefinitions to OpenAPI format
        List<Map<String, Object>> openApiFunctions = new ArrayList<>();
        
        for (FunctionDefinition func : agent.getAvailableFunctions()) {
            Map<String, Object> properties = new LinkedHashMap<>();
            
            // Convert each parameter to OpenAPI schema
            for (FunctionDefinition.Parameter param : func.getParameters()) {
                properties.put(param.getName(), Map.of(
                    "type", param.getType(),
                    "description", param.getDescription()
                ));
            }
            
            // OpenAPI function definition
            openApiFunctions.add(Map.of(
                "operationId", func.getName(),
                "summary", func.getDescription(),
                "requestBody", Map.of(
                    "content", Map.of(
                        "application/json", Map.of(
                            "schema", Map.of(
                                "type", "object",
                                "properties", properties
                            )
                        )
                    )
                )
            ));
        }
        
        return Map.of("functions", openApiFunctions);
    }
    
    private Object executeFunctionInternal(FunctionCall call) {
        // In reality, this would be the full REST-specific implementation
        // For demo, we just call the source directly
        return switch (call.getName()) {
            case "fetchLogs" -> {
                String resource = (String) call.getArguments().get("resource");
                String filter = (String) call.getArguments().get("filter");
                Integer limit = (Integer) call.getArguments().get("limit");
                yield source.fetchLogs(resource, filter, limit);
            }
            case "fetchMetrics" -> {
                String resource = (String) call.getArguments().get("resource");
                String metricName = (String) call.getArguments().get("metricName");
                String timeRange = (String) call.getArguments().get("timeRange");
                yield source.fetchMetrics(resource, metricName, timeRange);
            }
            default -> throw new IllegalArgumentException("Unknown function: " + call.getName());
        };
    }
}