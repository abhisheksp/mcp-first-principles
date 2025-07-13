package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for WatchTower.AI Agent
 * Branch 01-aws-mvp: Testing AWS-only troubleshooting implementation
 */
class WatchTowerAgentTest {
    
    protected WatchTowerAgent agent;
    
    @BeforeEach
    void setup() {
        agent = new WatchTowerAgent();
    }
    
    @Test
    @DisplayName("Troubleshoot: Why are payment APIs failing?")
    void troubleshootPaymentFailures() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("timeout")
            .contains("connection pool")
            .contains("Recommendation");
    }
}