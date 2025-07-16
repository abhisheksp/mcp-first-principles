package com.watchtower.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests demonstrating the protocol in action
 */
class ProtocolTest {
    
    @Test
    @DisplayName("Protocol eliminates transport multiplication")
    void showProtocolBenefits() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> PROTOCOL BENEFITS");
        System.out.println("=".repeat(60));
        
        System.out.println("\nBEFORE (Transport Multiplication):");
        System.out.println("- AWS REST API");
        System.out.println("- AWS gRPC Service");
        System.out.println("- AWS CLI");
        System.out.println("- GCP REST API");
        System.out.println("- GCP gRPC Service");
        System.out.println("- GCP CLI");
        System.out.println("Total: 6 implementations");
        
        System.out.println("\nAFTER (Protocol):");
        System.out.println("- AWS Protocol Server");
        System.out.println("- GCP Protocol Server");
        System.out.println("- One Protocol Client");
        System.out.println("Total: 3 implementations");
        
        System.out.println("\nReduction: 50%!");
    }
    
    @Test
    @DisplayName("Same protocol message format for all sources")
    void showStandardMessages() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> STANDARD MESSAGE FORMAT");
        System.out.println("=".repeat(60));
        
        // Show standard request format
        ProtocolRequest request = ProtocolRequest.builder()
            .jsonrpc("2.0")
            .method("execute")
            .params(Map.of(
                "operation", "fetchLogs",
                "arguments", Map.of(
                    "resource", "payment-service",
                    "filter", "ERROR",
                    "limit", 100
                )
            ))
            .id("test-123")
            .build();
            
        System.out.println("\nStandard Request (works with ANY source):");
        System.out.println(request);
        
        // Show standard response format
        ProtocolResponse response = ProtocolResponse.success("test-123", Map.of(
            "logs", "...",
            "count", 100
        ));
        
        System.out.println("\nStandard Response (from ANY source):");
        System.out.println(response);
        
        System.out.println("\n>>> One format for all!");
    }
}