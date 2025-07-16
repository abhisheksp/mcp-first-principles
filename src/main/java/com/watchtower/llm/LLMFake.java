package com.watchtower.llm;

import com.watchtower.fakes.FakeDataLoader;
import com.watchtower.functions.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Enhanced LLM fake that supports function calling
 */
public class LLMFake {
    private static final String LLM_RESOURCE_PATH = "/llm/";
    
    // Simulated conversation state
    private List<Map<String, Object>> conversation = new ArrayList<>();
    private int functionCallCount = 0;
    
    /**
     * Complete with function calling support
     */
    public LLMResponse completeWithFunctions(
            List<Map<String, Object>> messages,
            List<FunctionDefinition> availableFunctions) {
        
        // Add messages to conversation
        conversation.addAll(messages);
        
        // Simulate LLM decision making based on conversation
        return simulateLLMResponse(availableFunctions);
    }
    
    private LLMResponse simulateLLMResponse(List<FunctionDefinition> functions) {
        // For demo: simulate intelligent function calling based on conversation
        functionCallCount++;
        
        // First call: always fetch error logs
        if (functionCallCount == 1) {
            return LLMResponse.functionCall(
                "I'll investigate the issue. Let me start by checking error logs.",
                new FunctionCall("fetchLogs", Map.of(
                    "resource", "payment-service",
                    "filter", "ERROR",
                    "limit", 1000
                ))
            );
        }
        
        // Second call: check metrics
        if (functionCallCount == 2) {
            return LLMResponse.functionCall(
                "I see errors in the logs. Let me check the error rate metrics.",
                new FunctionCall("fetchMetrics", Map.of(
                    "resource", "payment-service",
                    "metricName", "error_rate",
                    "timeRange", "1h"
                ))
            );
        }
        
        // Third call: check CPU
        if (functionCallCount == 3) {
            return LLMResponse.functionCall(
                "Error rate is elevated. Let me check CPU usage.",
                new FunctionCall("fetchMetrics", Map.of(
                    "resource", "payment-service",
                    "metricName", "cpu_usage",
                    "timeRange", "1h"
                ))
            );
        }
        
        // Fourth call: check database
        if (functionCallCount == 4) {
            return LLMResponse.functionCall(
                "High CPU detected. Let me check the database service.",
                new FunctionCall("fetchLogs", Map.of(
                    "resource", "database-service",
                    "filter", "WARN",
                    "limit", 500
                ))
            );
        }
        
        // Final response
        return LLMResponse.finalAnswer(
            "Based on my investigation:\n\n" +
            "**Root Cause**: Database connection pool exhaustion is causing payment service issues.\n\n" +
            "**Evidence**:\n" +
            "1. Payment service showing timeout errors\n" +
            "2. Error rate increased to 15% in the last hour\n" +
            "3. CPU usage at 85% due to retry attempts\n" +
            "4. Database logs show connection pool exhaustion\n\n" +
            "**Recommendation**: Increase database connection pool size and implement circuit breaker."
        );
    }
    
    // Backward compatibility
    public String complete(String promptType, String userQuery, String data) {
        try {
            String resourcePath = String.format("/llm/%s-response.txt", promptType);
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            // Ignore
        }
        
        String filename = switch (promptType.toLowerCase()) {
            case "troubleshoot" -> "troubleshoot-payment-errors.txt";
            case "summary" -> "summary-daily-activity.txt";
            case "anomaly" -> "anomaly-traffic-spike.txt";
            default -> throw new IllegalArgumentException("Unknown prompt type: " + promptType);
        };
        
        String template = FakeDataLoader.loadTextFromResource(LLM_RESOURCE_PATH + filename);
        
        // Add formatting to make it look like real LLM output
        return String.format(
            "Query: %s\n\n" +
            "Based on the provided data:\n" +
            "```\n%s\n```\n\n" +
            "%s",
            userQuery,
            data.length() > 200 ? data.substring(0, 200) + "..." : data,
            template
        );
    }
    
    public void reset() {
        conversation.clear();
        functionCallCount = 0;
    }
}