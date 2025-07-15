package com.watchtower.sources;

import com.watchtower.fakes.AWSCloudWatchFake;
import com.watchtower.model.LogEntry;
import com.watchtower.model.Metric;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * AWS CloudWatch implementation of CloudLogSource
 */
@Slf4j
public class AWSLogSource implements CloudLogSource {
    
    private AWSCloudWatchFake client;
    
    @Override
    public void initialize(Map<String, String> config) {
        log.info("Initializing AWS CloudWatch source");
        
        Map<String, Optional<String>> awsConfig = Map.of(
            "accessKeyId", Optional.ofNullable(config.get("accessKeyId")),
            "secretAccessKey", Optional.ofNullable(config.get("secretAccessKey")),
            "region", Optional.ofNullable(config.getOrDefault("region", "us-east-1"))
        );
        
        this.client = new AWSCloudWatchFake(awsConfig);
    }
    
    @Override
    public List<LogEntry> fetchLogs(String logicalResource, String filter, int limit) {
        // Translate logical names to AWS log group names
        String logGroup = translateToAWSLogGroup(logicalResource);
        return client.filterLogEvents(logGroup, filter, limit);
    }
    
    @Override
    public List<Metric> fetchMetrics(String logicalResource, String metricName, String timeRange) {
        // Translate logical names to AWS CloudWatch metrics
        String namespace = translateToAWSNamespace(logicalResource);
        String awsMetricName = translateToAWSMetric(metricName);
        
        // In real implementation, would call CloudWatch Metrics API
        // For demo, return fake metrics
        return generateFakeMetrics(namespace, awsMetricName, timeRange);
    }
    
    @Override
    public String getCloudProvider() {
        return "AWS";
    }
    
    private String translateToAWSLogGroup(String logicalResource) {
        return switch (logicalResource) {
            case "payment-service" -> "/aws/payment-service";
            case "user-service" -> "/aws/user-service";
            default -> "/aws/" + logicalResource;
        };
    }
    
    private String translateToAWSNamespace(String logicalResource) {
        return switch (logicalResource) {
            case "payment-service" -> "AWS/ApplicationELB";
            case "user-service" -> "AWS/ApplicationELB";
            default -> "AWS/EC2";
        };
    }
    
    private String translateToAWSMetric(String metricName) {
        return switch (metricName) {
            case "error_rate" -> "HTTPCode_Target_5XX_Count";
            case "cpu_usage" -> "CPUUtilization";
            case "request_count" -> "RequestCount";
            default -> metricName;
        };
    }
    
    private List<Metric> generateFakeMetrics(String namespace, String metricName, String timeRange) {
        // Generate some fake metrics for demo
        List<Metric> metrics = new ArrayList<>();
        Instant now = Instant.now();
        
        // Generate data points based on time range
        int dataPoints = switch (timeRange) {
            case "1h" -> 12;   // 5-minute intervals
            case "24h" -> 24;  // Hourly
            case "7d" -> 7;    // Daily
            default -> 10;
        };
        
        for (int i = 0; i < dataPoints; i++) {
            metrics.add(new Metric(
                now.minus(i * 5, ChronoUnit.MINUTES),
                metricName,
                Math.random() * 100,  // Random value for demo
                "Count",
                Map.of("namespace", namespace)
            ));
        }
        
        return metrics;
    }
}