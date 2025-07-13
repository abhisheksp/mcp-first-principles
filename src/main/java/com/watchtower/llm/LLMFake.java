package com.watchtower.llm;

import com.watchtower.fakes.FakeDataLoader;

public class LLMFake {
    private static final String LLM_RESOURCE_PATH = "/llm/";
    
    public String complete(String promptType, String userQuery, String data) {
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
}