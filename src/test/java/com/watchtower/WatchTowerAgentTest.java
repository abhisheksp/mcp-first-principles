package com.watchtower;

import com.watchtower.model.Metric;
import com.watchtower.sources.CloudLogSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

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
    @DisplayName("Analyze AWS: Why are payment APIs failing?")
    void analyzePaymentFailuresAWS() {
        String analysis = agent.analyze(
            "Payment API returning 500 errors in last hour",
            "AWS"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> AWS ANALYSIS COMPLETE");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("Root Cause")
            .contains("Database connection pool");
    }
    
    @Test
    @DisplayName("Analyze GCP: Why are payment APIs failing?")
    void analyzePaymentFailuresGCP() {
        String analysis = agent.analyze(
            "Payment API returning 500 errors in last hour",
            "GCP"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> GCP ANALYSIS COMPLETE");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("Root Cause")
            .contains("Database connection pool");
    }
    
    @Test
    @DisplayName("Verify metrics fetching works for all providers")
    void testMetricsFetching() {
        List<String> providers = List.of("AWS", "GCP");
        
        for (String provider : providers) {
            CloudLogSource source = agent.sources.get(provider);
            List<Metric> metrics = source.fetchMetrics("payment-service", "error_rate", "1h");
            
            System.out.println("\n>>> " + provider + " Metrics Sample:");
            metrics.stream().limit(3).forEach(m -> 
                System.out.printf("  [%s] %s: %.2f %s%n", 
                    m.getTimestamp(), m.getName(), m.getValue(), m.getUnit())
            );
            
            assertThat(metrics).isNotEmpty();
        }
    }
}