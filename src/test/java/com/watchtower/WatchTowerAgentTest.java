package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

class WatchTowerAgentTest {
    
    protected WatchTowerAgent agent;
    
    @BeforeEach
    void setup() {
        System.setProperty("AWS_ACCESS_KEY_ID", "fake");
        System.setProperty("AWS_SECRET_ACCESS_KEY", "fake");
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "fake");
        System.setProperty("AZURE_SUBSCRIPTION_ID", "fake");
        System.setProperty("AZURE_TENANT_ID", "fake");
        System.setProperty("AZURE_CLIENT_ID", "fake");
        System.setProperty("AZURE_CLIENT_SECRET", "fake");
        
        agent = new WatchTowerAgent();
    }
    
    @Test
    @DisplayName("Discover capabilities across all sources")
    void discoverCapabilities() {
        var capabilities = agent.discoverCapabilities();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> CAPABILITY DISCOVERY");
        System.out.println("=".repeat(60));
        
        capabilities.forEach((provider, caps) -> {
            System.out.println("\n" + provider + ":");
            System.out.println("  Description: " + caps.getDescription());
            System.out.println("  Operations: " + caps.getSupportedOperations());
            System.out.println("  Resources: " + caps.getSupportedResources());
            System.out.println("  Filters: " + caps.getSupportedFilters());
            System.out.println("  Time Ranges: " + caps.getSupportedTimeRanges());
        });
        
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(capabilities).hasSize(3);
        // AWS has the most capabilities
        assertThat(capabilities.get("AWS").getSupportedOperations()).hasSize(4);
        assertThat(capabilities.get("GCP").getSupportedOperations()).hasSize(2);
        assertThat(capabilities.get("AZURE").getSupportedOperations()).hasSize(1);
    }
    
    @Test
    @DisplayName("Troubleshoot AWS: Payment API failures")
    void troubleshootPaymentFailuresAWS() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "AWS"
        );
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> AWS TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(60));
        System.out.println(analysis);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(analysis).contains("AWS").contains("timeout").contains("connection pool");
    }
    
    @Test
    @DisplayName("Summary AWS: Daily activity report")
    void generateDailySummaryAWS() {
        String summary = agent.generateSummary("24h", "AWS");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> AWS DAILY SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println(summary);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(summary).contains("AWS").contains("24h").contains("metrics: true");
    }
    
    @Test
    @DisplayName("Anomaly AWS: Detect traffic spikes")
    void detectAnomaliesAWS() {
        String anomalies = agent.detectAnomalies(
            "baseline: 1000 req/min",
            "AWS"
        );
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> AWS ANOMALY DETECTION");
        System.out.println("=".repeat(60));
        System.out.println(anomalies);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(anomalies).contains("AWS").contains("baseline: 1000 req/min");
    }
    
    @Test
    @DisplayName("Troubleshoot GCP: Payment API failures")
    void troubleshootPaymentFailuresGCP() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "GCP"
        );
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> GCP TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(60));
        System.out.println(analysis);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(analysis).contains("GCP").contains("timeout");
    }
    
    @Test
    @DisplayName("Summary GCP: Daily activity report")
    void generateDailySummaryGCP() {
        String summary = agent.generateSummary("24h", "GCP");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> GCP DAILY SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println(summary);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(summary).contains("GCP").contains("24h");
    }
    
    @Test
    @DisplayName("Anomaly GCP: Limited data but still attempts")
    void detectAnomaliesGCPLimitedData() {
        // GCP only supports up to 7d, but agent should still try with available data
        String anomalies = agent.detectAnomalies("baseline: 1000 req/min", "GCP");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> GCP ANOMALY DETECTION (LIMITED DATA)");
        System.out.println("=".repeat(60));
        System.out.println(anomalies);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(anomalies).contains("GCP").contains("limited historical data");
    }
    
    @Test
    @DisplayName("Troubleshoot Azure: Payment API failures")
    void troubleshootPaymentFailuresAzure() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour",
            "AZURE"
        );
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> AZURE TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(60));
        System.out.println(analysis);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(analysis).contains("AZURE");
    }
    
    @Test
    @DisplayName("Summary Azure: Falls back to supported time range")
    void generateDailySummaryAzureFallback() {
        // Azure only supports 24h, but agent should handle gracefully
        String summary = agent.generateSummary("7d", "AZURE");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(">>> AZURE SUMMARY (FALLBACK TO 24h)");
        System.out.println("=".repeat(60));
        System.out.println(summary);
        System.out.println("=".repeat(60) + "\n");
        
        assertThat(summary).contains("AZURE").contains("24h"); // Falls back to 24h
    }
}