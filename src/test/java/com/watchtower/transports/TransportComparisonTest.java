package com.watchtower.transports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shows the same function being called through different transports
 */
class TransportComparisonTest {
    
    @Test
    @DisplayName("Same function, different transports")
    void compareDifferentTransports() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> SAME FUNCTION THROUGH DIFFERENT TRANSPORTS");
        System.out.println("=".repeat(60));
        
        // REST call
        System.out.println("\n--- REST API Call ---");
        AWSCloudLogSourceREST restApi = new AWSCloudLogSourceREST();
        Map<String, Object> restRequest = Map.of(
            "function", "fetchLogs",
            "arguments", Map.of(
                "resource", "payment-service",
                "filter", "ERROR",
                "limit", 10
            )
        );
        
        Map<String, Object> restResponse = restApi.executeFunction(restRequest);
        System.out.println("REST Request: " + restRequest);
        System.out.println("REST Response: " + restResponse.get("status"));
        
        // gRPC call
        System.out.println("\n--- gRPC Call ---");
        GCPCloudLogSourceGRPC grpcService = new GCPCloudLogSourceGRPC();
        Map<String, Object> grpcRequest = Map.of(
            "function_name", "fetchLogs",  // Different field name!
            "arguments", Map.of(
                "resource", "payment-service",
                "filter", "ERROR",
                "limit", 10
            )
        );
        
        Map<String, Object> grpcResponse = grpcService.executeFunction(grpcRequest);
        System.out.println("gRPC Request: " + grpcRequest);
        System.out.println("gRPC Response: " + grpcResponse.get("status"));
        
        System.out.println("\n>>> Notice: Different request formats for same operation!");
    }
    
    @Test
    @DisplayName("Function discovery in different formats")
    void compareFunctionDiscovery() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> FUNCTION DISCOVERY FORMATS");
        System.out.println("=".repeat(60));
        
        // REST discovery
        System.out.println("\n--- REST (OpenAPI) Format ---");
        AWSCloudLogSourceREST restApi = new AWSCloudLogSourceREST();
        Map<String, Object> restFunctions = restApi.getFunctions();
        System.out.println("OpenAPI Functions: " + restFunctions);
        
        // gRPC discovery
        System.out.println("\n--- gRPC (Protobuf) Format ---");
        GCPCloudLogSourceGRPC grpcService = new GCPCloudLogSourceGRPC();
        Map<String, Object> grpcFunctions = grpcService.listFunctions();
        System.out.println("Protobuf Functions: " + grpcFunctions);
        
        System.out.println("\n>>> Same functions, completely different formats!");
    }
}