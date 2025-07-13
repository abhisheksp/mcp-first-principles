package com.watchtower;

import com.watchtower.fakes.AWSCloudWatchFake;
import com.watchtower.fakes.GCPLoggingFake;
import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - Now with AWS AND GCP support!
 * 
 * Notice how quickly this is getting messy:
 * - Two different clients
 * - Two different auth patterns
 * - If-else logic everywhere
 * - What happens when we add Azure?
 */
@Slf4j
public class WatchTowerAgent {
    // Now we have TWO clients!
    private final AWSCloudWatchFake awsClient;
    private final GCPLoggingFake gcpClient;
    private final LLMFake llm;
    
    public WatchTowerAgent() {
        // This constructor is getting out of hand...
        
        // AWS Authentication
        Map<String, Optional<String>> awsCredentials = Map.of(
            "accessKeyId", Optional.ofNullable(System.getenv("AWS_ACCESS_KEY_ID")),
            "secretAccessKey", Optional.ofNullable(System.getenv("AWS_SECRET_ACCESS_KEY")),
            "region", Optional.of("us-east-1")
        );
        
        System.out.println(">>> Authenticating with AWS...");
        this.awsClient = new AWSCloudWatchFake(awsCredentials);
        
        // GCP Authentication - completely different pattern!
        String gcpServiceAccount = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        
        System.out.println(">>> Authenticating with GCP...");
        this.gcpClient = new GCPLoggingFake(gcpServiceAccount);
        
        // Initialize LLM
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized with AWS and GCP support");
    }
    
    // Now we need a provider parameter! 🚨
    public String troubleshootErrors(String userQuery, String cloudProvider) {
        System.out.println(">>> Troubleshooting on " + cloudProvider + ": " + userQuery);
        
        List<LogEntry> logs;
        
        // The if-else mess begins... 🚨
        if ("AWS".equals(cloudProvider)) {
            // AWS-specific API call
            logs = awsClient.filterLogEvents(
                "/aws/payment-service",
                "ERROR",
                1000
            );
        } else if ("GCP".equals(cloudProvider)) {
            // GCP-specific API call - different method name, different parameters!
            logs = gcpClient.listLogEntries(
                "projects/my-gcp-project/logs/payment-service",
                "severity=\"ERROR\"",
                1000
            );
        } else {
            throw new IllegalArgumentException("Unsupported cloud provider: " + cloudProvider);
        }
        
        // At least this part is the same...
        String logData = logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
        
        // But wait, we should tell the LLM which cloud it's analyzing!
        return llm.complete("troubleshoot", 
            String.format("[%s] %s", cloudProvider, userQuery), 
            logData);
    }
    
    // TODO: Still need to add summary and anomaly detection...
    // TODO: What about Azure? This is already getting messy!
    // TODO: How do we handle different log formats per cloud?
    // TODO: The constructor is becoming a monster...
}