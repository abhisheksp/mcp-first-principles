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
    @DisplayName("Function calling analysis")
    void functionCallingAnalysis() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> FUNCTION CALLING ANALYSIS");
        System.out.println("=".repeat(60));
        
        String result = agent.analyze("Why are payments failing?", "AWS");
        System.out.println("\nFinal result:\n" + result);
        
        assertThat(result)
            .contains("Root Cause")
            .contains("Database connection pool");
        
        System.out.println("=".repeat(60));
    }
    
    @Test
    @DisplayName("Dynamic investigation with function calling")
    void dynamicInvestigation() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> DYNAMIC INVESTIGATION");
        System.out.println("=".repeat(60));
        
        String result = agent.analyze(
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
        agent.analyze("Check for any errors in payment service", "GCP");
        
        // Reset LLM state
        agent = new WatchTowerAgent();
        
        // Query 2: Performance focused  
        System.out.println("\n--- Query: Performance Investigation ---");
        agent.analyze("Analyze payment service performance", "GCP");
        
        System.out.println("\n>>> Notice: LLM chooses different function sequences!");
    }
}