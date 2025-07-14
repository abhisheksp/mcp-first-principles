package com.watchtower.llm;

import com.watchtower.fakes.FakeDataLoader;

public class LLMFake {
    private static final String LLM_RESOURCE_PATH = "/llm/";
    
    public String complete(String promptType, String userQuery, String data) {
        return complete(promptType, userQuery, data, null);
    }
    
    public String complete(String promptType, String userQuery, String data, String systemPrompt) {
        String filename = switch (promptType.toLowerCase()) {
            case "troubleshoot" -> "troubleshoot-payment-errors.txt";
            case "summary" -> "summary-daily-activity.txt";
            case "anomaly" -> "anomaly-traffic-spike.txt";
            default -> throw new IllegalArgumentException("Unknown prompt type: " + promptType);
        };
        
        String template = FakeDataLoader.loadTextFromResource(LLM_RESOURCE_PATH + filename);
        
        // If system prompt provided, modify response to reflect transport specialization
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            template = addTransportSpecificInsights(template, systemPrompt);
        }
        
        // Add formatting to make it look like real LLM output
        return String.format(
            "Query: %s\n\n" +
            (systemPrompt != null ? "System: Using specialized %s analysis\n\n" : "") +
            "Based on the provided data:\n" +
            "```\n%s\n```\n\n" +
            "%s",
            userQuery,
            systemPrompt != null ? getTransportType(systemPrompt) : "",
            data.length() > 200 ? data.substring(0, 200) + "..." : data,
            template
        );
    }
    
    private String addTransportSpecificInsights(String template, String systemPrompt) {
        // Add transport-specific insights based on system prompt
        if (systemPrompt.contains("REST API specialist")) {
            return template + "\n\n🌐 REST-specific insight: Consider HTTP status code patterns and OAuth token refresh cycles.";
        } else if (systemPrompt.contains("gRPC specialist")) {
            return template + "\n\n⚡ gRPC-specific insight: Monitor connection health and Protocol Buffer schema compatibility.";
        } else if (systemPrompt.contains("CLI specialist")) {
            return template + "\n\n💻 CLI-specific insight: Check exit codes and environment variable configurations.";
        } else if (systemPrompt.contains("WebSocket specialist")) {
            return template + "\n\n🔄 WebSocket-specific insight: Analyze connection state changes and message framing issues.";
        } else if (systemPrompt.contains("Kafka specialist")) {
            return template + "\n\n📨 Kafka-specific insight: Review consumer lag metrics and partition rebalancing patterns.";
        }
        
        return template;
    }
    
    private String getTransportType(String systemPrompt) {
        if (systemPrompt.contains("REST API specialist")) return "REST/HTTP";
        if (systemPrompt.contains("gRPC specialist")) return "gRPC/HTTP2";
        if (systemPrompt.contains("CLI specialist")) return "CLI/Process";
        if (systemPrompt.contains("WebSocket specialist")) return "WebSocket/Real-time";
        if (systemPrompt.contains("Kafka specialist")) return "Kafka/Message Queue";
        return "Generic";
    }
    
    // In LLMFake.java, add:
    public String completeWithSystemPrompt(String systemPrompt, String userQuery, String data) {
        // In a real implementation, this would:
        // 1. Combine system prompt + user query + data
        // 2. Send to LLM API with proper role separation
        // 3. Return the structured response
        
        // For our demo, just indicate which prompt was used
        // Infer the prompt type from the system prompt content
        String promptType = "troubleshoot"; // Default
        if (systemPrompt.contains("technical analyst") || systemPrompt.contains("executive dashboards")) {
            promptType = "summary";
        } else if (systemPrompt.contains("security analyst") || systemPrompt.contains("threat detection")) {
            promptType = "anomaly";
        }
        
        return String.format("[Using %s persona]\n%s", 
            systemPrompt.substring(0, 50) + "...", 
            complete(promptType, userQuery, data));
    }
}