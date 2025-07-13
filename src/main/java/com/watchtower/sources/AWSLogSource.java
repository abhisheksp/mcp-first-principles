package com.watchtower.sources;

import com.watchtower.fakes.AWSCloudWatchFake;
import com.watchtower.model.LogEntry;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AWS CloudWatch adapter implementing our common interface
 */
@RequiredArgsConstructor
public class AWSLogSource implements CloudLogSource {
    private AWSCloudWatchFake client;
    
    @Override
    public void initialize(Map<String, String> config) {
        System.out.println(">>> Initializing AWS Log Source...");
        
        // AWS-specific credential setup
        Map<String, Optional<String>> awsCredentials = Map.of(
            "accessKeyId", Optional.ofNullable(config.get("AWS_ACCESS_KEY_ID")),
            "secretAccessKey", Optional.ofNullable(config.get("AWS_SECRET_ACCESS_KEY")),
            "region", Optional.ofNullable(config.getOrDefault("AWS_REGION", "us-east-1"))
        );
        
        this.client = new AWSCloudWatchFake(awsCredentials);
    }
    
    @Override
    public List<LogEntry> fetchLogs(String logicalResource, String filter, int limit) {
        // Translate logical resource to AWS-specific format
        String awsLogGroup = translateToAWSLogGroup(logicalResource);
        
        // Adapt our interface to AWS-specific API
        return client.filterLogEvents(awsLogGroup, filter, limit);
    }
    
    private String translateToAWSLogGroup(String logicalResource) {
        // Map logical names to AWS log groups
        return switch (logicalResource) {
            case "payment-service" -> "/aws/payment-service";
            case "user-service" -> "/aws/user-service";
            case "order-service" -> "/aws/order-service";
            default -> "/aws/" + logicalResource; // Fallback pattern
        };
    }
    
    @Override
    public String getCloudProvider() {
        return "AWS";
    }
    
    @Override
    public SourceCapabilities getCapabilities() {
        return SourceCapabilities.builder()
            .provider("AWS")
            .description("AWS CloudWatch Logs - Full featured logging and metrics")
            .supportedOperations(List.of("fetchLogs", "fetchMetrics", "streamLogs", "exportLogs"))
            .supportedResources(List.of("payment-service", "user-service", "order-service", "inventory-service"))
            .supportedFilters(List.of("ERROR", "WARN", "INFO", "DEBUG"))
            .supportedTimeRanges(List.of("1h", "24h", "7d", "30d", "90d"))
            .metadata(Map.of(
                "region", "us-east-1",
                "retentionDays", "90",
                "features", "insights,alarms,dashboards"
            ))
            .build();
    }
}