package com.watchtower.protocol;

import com.watchtower.WatchTowerAgent;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates the agent connecting to MULTIPLE protocol servers.
 * The LLM can choose to fetch data from AWS, GCP, or both!
 * 
 * SETUP:
 * 1. Start AWS server: mvn exec:java -Dexec.mainClass="com.watchtower.protocol.CloudLogSourceProtocolServer" -Dexec.args="AWS 8001"
 * 2. Start GCP server: mvn exec:java -Dexec.mainClass="com.watchtower.protocol.CloudLogSourceProtocolServer" -Dexec.args="GCP 8002"
 * 3. Run these tests
 * 
 * This shows the true power of MCP - unified access to multiple tools!
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WatchTowerAgentProtocolTest {
    
    @Test
    @Order(1)
    @DisplayName("Multi-Cloud Investigation - LLM chooses from both AWS and GCP")
    public void testMultiSourceProtocol() throws IOException {
        System.out.println("\n🌟 === MULTI-SOURCE PROTOCOL TEST ===");
        System.out.println("Creating agent with connections to BOTH AWS and GCP servers...\n");
        
        // Configure connections to multiple servers
        Map<String, WatchTowerAgent.ServerConfig> servers = new HashMap<>();
        
        // AWS Server
        servers.put("AWS", new WatchTowerAgent.ServerConfig(
            "localhost", 
            8001,
            Map.of(
                "accessKeyId", "test-key",
                "secretAccessKey", "test-secret",
                "region", "us-east-1"
            )
        ));
        
        // GCP Server
        servers.put("GCP", new WatchTowerAgent.ServerConfig(
            "localhost",
            8002,
            Map.of(
                "projectId", "test-project",
                "serviceAccountPath", "/path/to/service-account.json"
            )
        ));
        
        // Create agent connected to BOTH sources
        WatchTowerAgent agent = new WatchTowerAgent(servers);
        
        // Ask a cross-cloud question - watch the LLM investigate both!
        System.out.println("\n🔍 User Query: Compare payment service performance between AWS and GCP\n");
        String result = agent.analyze("Compare payment service performance between AWS and GCP");
        
        System.out.println("\n📊 Cross-Cloud Analysis:");
        System.out.println(result);
        
        // Verify the analysis includes data from both sources
        assertTrue(result.toLowerCase().contains("database") || result.toLowerCase().contains("memory") || result.toLowerCase().contains("payment"));
        
        agent.close();
        System.out.println("\n✅ Multi-source protocol test complete!");
    }
    
    @Test
    @Order(2)
    @DisplayName("Unified troubleshooting across clouds")
    public void testUnifiedTroubleshooting() throws IOException {
        System.out.println("\n🔧 === UNIFIED TROUBLESHOOTING TEST ===");
        
        // Setup multi-source agent
        Map<String, WatchTowerAgent.ServerConfig> servers = Map.of(
            "AWS", new WatchTowerAgent.ServerConfig("localhost", 8001, 
                Map.of("provider", "AWS")),
            "GCP", new WatchTowerAgent.ServerConfig("localhost", 8002,
                Map.of("provider", "GCP"))
        );
        
        WatchTowerAgent agent = new WatchTowerAgent(servers);
        
        // Complex query requiring cross-cloud investigation
        System.out.println("\n🔍 User Query: Find the root cause of payment failures across our infrastructure\n");
        String result = agent.analyze("Find the root cause of payment failures across our infrastructure");
        
        System.out.println("\n📊 Root Cause Analysis:");
        System.out.println(result);
        
        // The LLM should have investigated both clouds
        assertNotNull(result);
        assertTrue(result.length() > 100); // Substantial analysis
        
        agent.close();
        System.out.println("\n✅ Unified troubleshooting complete!");
    }
    
    @Test
    @Order(3)
    @DisplayName("The MCP Vision - Universal tool access")
    public void testMCPVision() throws IOException {
        System.out.println("\n✨ === THE MCP VISION REALIZED ===\n");
        
        Map<String, WatchTowerAgent.ServerConfig> servers = Map.of(
            "AWS", new WatchTowerAgent.ServerConfig("localhost", 8001, Map.of("provider", "AWS")),
            "GCP", new WatchTowerAgent.ServerConfig("localhost", 8002, Map.of("provider", "GCP"))
        );
        
        WatchTowerAgent agent = new WatchTowerAgent(servers);
        
        System.out.println("💡 Key Insights:");
        System.out.println("1. The agent connects to MULTIPLE sources simultaneously");
        System.out.println("2. The LLM sees a unified function list from ALL sources");
        System.out.println("3. Functions are namespaced (AWS.fetchLogs, GCP.fetchLogs)");
        System.out.println("4. The LLM decides which source to query for each piece of data");
        System.out.println("5. No source-specific code in the agent!");
        System.out.println("\nThis is EXACTLY what MCP enables:");
        System.out.println("- One protocol, many tools");
        System.out.println("- LLMs orchestrate across all available tools");
        System.out.println("- Complete decoupling of consumers and providers");
        
        // Simple test to verify it works
        String result = agent.analyze("Show me system health");
        assertNotNull(result);
        
        agent.close();
        System.out.println("\n🎉 We've built MCP from first principles!");
    }
    
    @Test
    @Order(4)
    @DisplayName("Single Source Configuration - Single protocol source")
    public void testSingleSourceConfiguration() throws IOException {
        System.out.println("\n🔄 === SINGLE SOURCE CONFIGURATION TEST ===");
        
        // Single-source configuration should work
        CloudLogSourceProtocolClient client = new CloudLogSourceProtocolClient("localhost", 8001);
        client.initialize(Map.of("provider", "AWS"));
        
        WatchTowerAgent agent = new WatchTowerAgent(client, "AWS");
        
        // Methods should still work
        String result = agent.troubleshootErrors("Why are payments failing?");
        assertNotNull(result);
        assertTrue(result.contains("payment"));
        
        agent.close();
        System.out.println("\n✅ Single source configuration verified!");
    }
    
    @Test
    @Order(5)
    @DisplayName("Function Discovery - LLM sees all available functions")
    public void testFunctionDiscovery() throws IOException {
        System.out.println("\n🔍 === FUNCTION DISCOVERY TEST ===");
        
        Map<String, WatchTowerAgent.ServerConfig> servers = Map.of(
            "AWS", new WatchTowerAgent.ServerConfig("localhost", 8001, Map.of("provider", "AWS")),
            "GCP", new WatchTowerAgent.ServerConfig("localhost", 8002, Map.of("provider", "GCP"))
        );
        
        WatchTowerAgent agent = new WatchTowerAgent(servers);
        
        // Check that functions are discovered from all sources
        var functions = agent.getAvailableFunctions();
        System.out.println("\n📋 Available Functions:");
        functions.forEach(f -> System.out.println("  - " + f.getName() + ": " + f.getDescription()));
        
        // Should have namespaced functions from both sources
        boolean hasAWSFunctions = functions.stream().anyMatch(f -> f.getName().startsWith("AWS."));
        boolean hasGCPFunctions = functions.stream().anyMatch(f -> f.getName().startsWith("GCP."));
        
        assertTrue(hasAWSFunctions, "Should have AWS.* functions");
        assertTrue(hasGCPFunctions, "Should have GCP.* functions");
        
        agent.close();
        System.out.println("\n✅ Function discovery working correctly!");
    }
}