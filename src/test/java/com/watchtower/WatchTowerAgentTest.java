package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for WatchTower.AI Agent
 * Branch 02-gcp-pressure: Testing AWS and GCP implementations
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
    @DisplayName("Troubleshoot AWS: Why are payment APIs failing?")
    void troubleshootPaymentFailuresAWS() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "AWS"  // Now we need to specify the provider!
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> AWS TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("AWS")  // Should indicate which cloud
            .contains("timeout")
            .contains("connection pool")
            .contains("Recommendation");
    }
    
    @Test
    @DisplayName("Troubleshoot GCP: Why are payment APIs failing?")
    void troubleshootPaymentFailuresGCP() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "GCP"  // Same query, different cloud!
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> GCP TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("GCP")  // Should indicate which cloud
            .contains("timeout")
            .contains("connection pool")
            .contains("Recommendation");
    }
}