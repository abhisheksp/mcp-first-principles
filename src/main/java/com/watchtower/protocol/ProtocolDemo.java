package com.watchtower.protocol;

import java.util.Map;

/**
 * Demonstrates the CloudLogSource Protocol in action
 */
public class ProtocolDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println(">>> CloudLogSource Protocol Demo");
        System.out.println(">>> =============================");
        System.out.println();
        System.out.println(">>> ONE client for ALL sources!");
        System.out.println();
        
        // Demo with AWS
        demoSource("AWS", "java -cp target/classes com.watchtower.protocol.servers.AWSProtocolServerMain");
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Demo with GCP - same client!
        demoSource("GCP", "java -cp target/classes com.watchtower.protocol.servers.GCPProtocolServerMain");
        
        System.out.println("\n>>> Notice: Same client code works with both sources!");
        System.out.println(">>> This is the power of protocols!");
    }
    
    private static void demoSource(String name, String command) throws Exception {
        System.out.println(">>> Connecting to " + name + " via protocol...");
        
        try (CloudLogSourceProtocolClient client = CloudLogSourceProtocolClient.connect(command)) {
            
            // Initialize
            System.out.println("\n1. Initialize:");
            Map<String, Object> initResult = client.initialize();
            System.out.println("   Server: " + initResult.get("server_info"));
            
            // Discover
            System.out.println("\n2. Discover operations:");
            Map<String, Object> discoverResult = client.discover();
            System.out.println("   Available: " + discoverResult.get("operations"));
            
            // Execute
            System.out.println("\n3. Execute fetchLogs:");
            Map<String, Object> logsResult = client.execute("fetchLogs", Map.of(
                "resource", "payment-service",
                "filter", "ERROR",
                "limit", 5
            ));
            System.out.println("   Found " + logsResult.get("count") + " logs from " + logsResult.get("source"));
            
            System.out.println("\n4. Execute fetchMetrics:");
            Map<String, Object> metricsResult = client.execute("fetchMetrics", Map.of(
                "resource", "payment-service",
                "metricName", "error_rate",
                "timeRange", "1h"
            ));
            System.out.println("   Found " + metricsResult.get("count") + " metrics from " + metricsResult.get("source"));
        }
    }
}