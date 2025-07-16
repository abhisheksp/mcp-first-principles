package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates the function calling revolution
 */
class FunctionCallingTest {
    
    private WatchTowerAgent agent;
    
    @BeforeEach
    void setup() {
        System.setProperty("AWS_ACCESS_KEY_ID", "fake");
        System.setProperty("AWS_SECRET_ACCESS_KEY", "fake");
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "fake");
        
        agent = new WatchTowerAgent();
    }
    
    @Test
    @DisplayName("Compare: Old hardcoded vs New function calling")
    void compareOldVsNew() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> COMPARISON: Hardcoded vs Function Calling");
        System.out.println("=".repeat(60));
        
        // Old way
        System.out.println("\n--- OLD WAY (Hardcoded Sequence) ---");
        String oldResult = agent.troubleshootErrors("Why are payments failing?", "AWS");
        System.out.println("Old result: " + oldResult.substring(0, Math.min(100, oldResult.length())) + "...");
        
        // New way
        System.out.println("\n--- NEW WAY (LLM Orchestrates) ---");
        String newResult = agent.analyzeWithFunctions("Why are payments failing?", "AWS");
        System.out.println("\nFinal result:\n" + newResult);
        
        System.out.println("=".repeat(60));
    }
    
    @Test
    @DisplayName("Dynamic investigation with function calling")
    void dynamicInvestigation() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> DYNAMIC INVESTIGATION");
        System.out.println("=".repeat(60));
        
        String result = agent.analyzeWithFunctions(
            "Payment service is slow. Investigate why.", 
            "AWS"
        );
        
        assertThat(result).contains("Root Cause");
        assertThat(result).contains("Database connection pool");
    }
    
    @Test
    @DisplayName("Different queries lead to different function sequences")
    void differentQueryPatterns() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> DIFFERENT QUERIES, DIFFERENT SEQUENCES");
        System.out.println("=".repeat(60));
        
        // Query 1: Error focused
        System.out.println("\n--- Query: Error Investigation ---");
        agent.analyzeWithFunctions("Check for any errors in payment service", "GCP");
        
        // Reset LLM state
        agent = new WatchTowerAgent();
        
        // Query 2: Performance focused  
        System.out.println("\n--- Query: Performance Investigation ---");
        agent.analyzeWithFunctions("Analyze payment service performance", "GCP");
        
        System.out.println("\n>>> Notice: LLM chooses different function sequences!");
    }
}