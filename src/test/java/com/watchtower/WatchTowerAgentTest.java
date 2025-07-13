package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for WatchTower.AI Agent
 * Branch 03-extract-interface: Testing the cleaner interface-based approach
 */
class WatchTowerAgentTest {
    
    protected WatchTowerAgent agent;
    
    @BeforeEach
    void setup() {
        // Set up fake credentials for testing
        System.setProperty("AWS_ACCESS_KEY_ID", "fake-aws-key");
        System.setProperty("AWS_SECRET_ACCESS_KEY", "fake-aws-secret");
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "/path/to/fake-key.json");
        
        agent = new WatchTowerAgent();
    }
    
    @Test
    @DisplayName("Show available cloud providers")
    void showAvailableProviders() {
        var providers = agent.getAvailableProviders();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> AVAILABLE CLOUD PROVIDERS");
        System.out.println("=".repeat(50));
        providers.forEach(p -> System.out.println("  - " + p));
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(providers).containsExactlyInAnyOrder("AWS", "GCP");
    }
    
    @Test
    @DisplayName("Troubleshoot AWS: Why are payment APIs failing?")
    void troubleshootPaymentFailuresAWS() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "AWS"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> AWS TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("AWS")
            .contains("timeout")
            .contains("connection pool");
    }
    
    @Test
    @DisplayName("Troubleshoot GCP: Why are payment APIs failing?")
    void troubleshootPaymentFailuresGCP() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "GCP"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> GCP TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("GCP")
            .contains("timeout")
            .contains("connection pool");
    }
}