package com.watchtower;

import com.watchtower.transport.*;
import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates the transport multiplication problem:
 * One logical operation → Multiple transport implementations → Different challenges
 */
@Slf4j
public class TransportMultiplication {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println(">>> TRANSPORT MULTIPLICATION HELL");
        System.out.println("=".repeat(80));
        System.out.println();
        
        new TransportMultiplication().demonstrateTransportChaos();
    }
    
    public void demonstrateTransportChaos() {
        System.out.println("🎯 SCENARIO: Fetch logs from AWS CloudWatch");
        System.out.println("💀 REALITY: 5 different ways, 5 different problems");
        System.out.println();
        
        // Same logical operation, 5 different transport implementations
        demoRestTransport();
        demoGrpcTransport();
        demoCliTransport();
        demoWebSocketTransport();
        demoKafkaTransport();
        
        System.out.println("=".repeat(80));
        System.out.println(">>> TRANSPORT CHAOS SUMMARY");
        System.out.println("=".repeat(80));
        printTransportComparison();
    }
    
    private void demoRestTransport() {
        printSectionHeader("REST/HTTP Transport", "🌐");
        
        var restTransport = new RestTransport(
            "https://logs.us-east-1.amazonaws.com", 
            Map.of("auth_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        );
        
        try {
            List<LogEntry> logs = restTransport.queryLogs("payment-service", "ERROR", 100);
            System.out.println("✅ Success: Fetched " + logs.size() + " log entries");
            logs.forEach(log -> System.out.println("   📄 " + log.message()));
            
            System.out.println("\n🔧 AUTH INFO: " + restTransport.getAuthenticationInfo());
            System.out.println("🤖 SYSTEM PROMPT:");
            System.out.println(indentPrompt(restTransport.getSystemPrompt()));
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void demoGrpcTransport() {
        printSectionHeader("gRPC/HTTP2 Transport", "⚡");
        
        var grpcTransport = new GrpcTransport(
            "grpc://logs.us-east-1.amazonaws.com:443",
            Map.of("tls_enabled", "true", "service_account", "watchtower@aws.iam")
        );
        
        try {
            grpcTransport.connect();
            List<LogEntry> logs = grpcTransport.streamLogs("payment-service", "ERROR", 100);
            System.out.println("✅ Success: Streamed " + logs.size() + " log entries");
            logs.forEach(log -> System.out.println("   📄 " + log.message()));
            
            System.out.println("\n🔧 CONNECTION INFO: " + grpcTransport.getConnectionInfo());
            System.out.println("🤖 SYSTEM PROMPT:");
            System.out.println(indentPrompt(grpcTransport.getSystemPrompt()));
            
            grpcTransport.disconnect();
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void demoCliTransport() {
        printSectionHeader("CLI/Process Transport", "💻");
        
        var cliTransport = new CliTransport(
            "/usr/local/bin/aws",
            Map.of("config_path", "~/.aws/config", "profile", "watchtower")
        );
        
        try {
            List<LogEntry> logs = cliTransport.executeLogs("payment-service", "ERROR", 100);
            System.out.println("✅ Success: Executed CLI and parsed " + logs.size() + " log entries");
            logs.forEach(log -> System.out.println("   📄 " + log.message()));
            
            System.out.println("\n🔧 EXECUTION INFO: " + cliTransport.getExecutionInfo());
            System.out.println("🤖 SYSTEM PROMPT:");
            System.out.println(indentPrompt(cliTransport.getSystemPrompt()));
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void demoWebSocketTransport() {
        printSectionHeader("WebSocket/Real-time Transport", "🔄");
        
        var wsTransport = new WebSocketTransport(
            "wss://logs.us-east-1.amazonaws.com/stream",
            Map.of("ws_token", "ws_token_abc123", "origin", "https://watchtower.ai")
        );
        
        try {
            wsTransport.connect();
            List<LogEntry> logs = wsTransport.subscribeLogs("payment-service", "ERROR");
            System.out.println("✅ Success: Subscribed and received " + logs.size() + " log entries");
            logs.forEach(log -> System.out.println("   📄 " + log.message()));
            
            System.out.println("\n🔧 CONNECTION INFO: " + wsTransport.getConnectionInfo());
            System.out.println("🤖 SYSTEM PROMPT:");
            System.out.println(indentPrompt(wsTransport.getSystemPrompt()));
            
            wsTransport.disconnect();
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void demoKafkaTransport() {
        printSectionHeader("Kafka/Message Queue Transport", "📨");
        
        var kafkaTransport = new KafkaTransport(
            "kafka-cluster.us-east-1.amazonaws.com:9092",
            Map.of("consumer_group", "watchtower-logs", "offset_reset", "latest")
        );
        
        try {
            kafkaTransport.connect();
            List<LogEntry> logs = kafkaTransport.consumeLogs("payment-service", "ERROR", 100);
            System.out.println("✅ Success: Consumed " + logs.size() + " log entries from topic");
            logs.forEach(log -> System.out.println("   📄 " + log.message()));
            
            System.out.println("\n🔧 CLUSTER INFO: " + kafkaTransport.getClusterInfo());
            System.out.println("🤖 SYSTEM PROMPT:");
            System.out.println(indentPrompt(kafkaTransport.getSystemPrompt()));
            
            kafkaTransport.disconnect();
            
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private void printSectionHeader(String title, String emoji) {
        System.out.println(emoji + " " + title);
        System.out.println("-".repeat(title.length() + 3));
    }
    
    private String indentPrompt(String prompt) {
        return prompt.lines()
            .map(line -> "    " + line)
            .collect(java.util.stream.Collectors.joining("\n"));
    }
    
    private void printTransportComparison() {
        System.out.println("📊 TRANSPORT COMPLEXITY MATRIX:");
        System.out.println();
        
        String[][] matrix = {
            {"Transport", "Auth Method", "Data Format", "Error Handling", "Connection Mgmt"},
            {"REST", "Bearer Token", "JSON", "HTTP Status", "Stateless"},
            {"gRPC", "mTLS/JWT", "Protobuf", "gRPC Status", "Persistent"},
            {"CLI", "Config Files", "JSON/Text", "Exit Codes", "Process Spawn"},
            {"WebSocket", "Token + Origin", "JSON/Binary", "Close Codes", "Persistent + Heartbeat"},
            {"Kafka", "SASL/SSL", "Avro/JSON", "Consumer Errors", "Cluster Connection"}
        };
        
        for (String[] row : matrix) {
            System.out.printf("%-12s %-15s %-12s %-15s %-20s%n", 
                row[0], row[1], row[2], row[3], row[4]);
        }
        
        System.out.println();
        System.out.println("💡 KEY INSIGHT: Same logical operation → 5× implementation complexity");
        System.out.println("🚀 MCP SOLUTION: One protocol → Eliminate transport multiplication");
        System.out.println();
    }
}