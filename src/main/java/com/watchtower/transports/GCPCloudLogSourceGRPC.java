package com.watchtower.transports;

import com.watchtower.sources.GCPLogSource;
import com.watchtower.functions.*;
import com.watchtower.WatchTowerAgent;
import java.util.*;

/**
 * gRPC service wrapper for GCP CloudLogSource
 * 
 * Exposes CloudLogSource functions via gRPC
 */
public class GCPCloudLogSourceGRPC {
    private final GCPLogSource source;
    private final WatchTowerAgent agent;
    
    public GCPCloudLogSourceGRPC() {
        this.source = new GCPLogSource();
        this.source.initialize(Map.of(
            "serviceAccountPath", System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null ? System.getenv("GOOGLE_APPLICATION_CREDENTIALS") : "fake-path"
        ));
        this.agent = new WatchTowerAgent();
    }
    
    /**
     * gRPC method: ExecuteFunction
     * 
     * In reality, this would be generated from protobuf:
     * 
     * service CloudLogSource {
     *   rpc ExecuteFunction(FunctionRequest) returns (FunctionResponse);
     *   rpc ListFunctions(Empty) returns (FunctionList);
     * }
     */
    public Map<String, Object> executeFunction(Map<String, Object> grpcRequest) {
        // gRPC-specific: Parse protobuf message
        // In reality: FunctionRequest request = FunctionRequest.parseFrom(bytes);
        String functionName = (String) grpcRequest.get("function_name");
        Map<String, Object> arguments = (Map<String, Object>) grpcRequest.get("arguments");
        
        // gRPC-specific: mTLS authentication happens at connection level
        // Context ctx = Context.current();
        // validateClientCertificate(ctx);
        
        try {
            // Create function call
            FunctionCall call = new FunctionCall(functionName, arguments);
            
            // Execute
            // Note: In reality, this would handle:
            // - Protobuf serialization/deserialization
            // - gRPC status codes
            // - Streaming responses
            Object result = executeFunctionInternal(call);
            
            // gRPC-specific: Build protobuf response
            return Map.of(
                "function_name", functionName,
                "result", result,
                "status", "OK"
            );
            
        } catch (Exception e) {
            // gRPC-specific: Status codes and error details
            return Map.of(
                "error", Map.of(
                    "code", "INTERNAL",
                    "message", e.getMessage(),
                    "details", List.of() // Error details in protobuf format
                )
            );
        }
    }
    
    /**
     * gRPC method: ListFunctions
     * 
     * Returns function definitions in protobuf format
     */
    public Map<String, Object> listFunctions() {
        // gRPC-specific: Convert to protobuf message format
        List<Map<String, Object>> protoFunctions = new ArrayList<>();
        
        for (FunctionDefinition func : agent.getAvailableFunctions()) {
            List<Map<String, Object>> protoParams = new ArrayList<>();
            
            // Convert parameters to protobuf field definitions
            int fieldNumber = 1;
            for (FunctionDefinition.Parameter param : func.getParameters()) {
                protoParams.add(Map.of(
                    "field_number", fieldNumber++,
                    "field_name", param.getName(),
                    "field_type", convertToProtoType(param.getType()),
                    "description", param.getDescription()
                ));
            }
            
            protoFunctions.add(Map.of(
                "name", func.getName(),
                "description", func.getDescription(),
                "request_type", func.getName() + "Request",
                "response_type", func.getName() + "Response",
                "parameters", protoParams
            ));
        }
        
        return Map.of("functions", protoFunctions);
    }
    
    private String convertToProtoType(String javaType) {
        return switch (javaType) {
            case "string" -> "string";
            case "integer" -> "int32";
            case "boolean" -> "bool";
            default -> "string";
        };
    }
    
    private Object executeFunctionInternal(FunctionCall call) {
        // Similar to REST, but would handle protobuf serialization
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