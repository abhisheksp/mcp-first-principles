Current directory: /Users/abhishek/workspace/projects/mcp-first-principles
Current git state: main branch with foundation code complete

Create git branch 01-aws-mvp that implements the initial AWS-only version of WatchTower.AI.

Requirements:

1. First, create and checkout the branch:
```bash
git checkout -b 01-aws-mvp
```

2. Create WatchTowerAgent.java in src/main/java/com/watchtower/:
```java
package com.watchtower;

import com.watchtower.fakes.AWSCloudWatchFake;
import com.watchtower.llm.LLMFake;
import com.watchtower.model.LogEntry;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * WatchTower.AI Agent - MVP with AWS CloudWatch support only
 * 
 * This is our initial implementation - just troubleshooting for now.
 * Notice how everything is hardcoded to AWS.
 * This works great for our MVP, but what happens when customers want GCP support?
 */
public class WatchTowerAgent {
    private final AWSCloudWatchFake awsClient;
    private final LLMFake llm;
    
    public WatchTowerAgent() {
        // Hardcoded AWS setup - this is our MVP!
        
        // Step 1: AWS Authentication (in real world, this would be AWS SDK credentials)
        Map<String, String> awsCredentials = new HashMap<>();
        awsCredentials.put("accessKeyId", System.getenv("AWS_ACCESS_KEY_ID"));
        awsCredentials.put("secretAccessKey", System.getenv("AWS_SECRET_ACCESS_KEY"));
        awsCredentials.put("region", "us-east-1");
        
        System.out.println(">>> Authenticating with AWS...");
        this.awsClient = new AWSCloudWatchFake(awsCredentials);
        
        // Step 2: Initialize LLM
        this.llm = new LLMFake();
        
        System.out.println(">>> WatchTower.AI initialized with AWS CloudWatch support");
    }
    
    public String troubleshootErrors(String userQuery) {
        System.out.println(">>> Troubleshooting: " + userQuery);
        
        // Fetch error logs from AWS - hardcoded to payment service
        List<LogEntry> logs = awsClient.filterLogEvents(
            "/aws/payment-service",
            "ERROR",
            1000
        );
        
        // Convert to string for LLM
        String logData = logs.stream()
            .map(log -> String.format("[%s] %s", log.timestamp(), log.message()))
            .collect(Collectors.joining("\n"));
        
        // Use LLM to analyze
        return llm.complete("troubleshoot", userQuery, logData);
    }
    
    // TODO: Customers asking for more analysis types...
    // TODO: What about GCP logs?
    // TODO: How do we handle different auth patterns for different clouds?
}
```

3. Create AWSCloudWatchFake.java in src/main/java/com/watchtower/fakes/:
```java
package com.watchtower.fakes;

import com.watchtower.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fake AWS CloudWatch client that reads from test resources
 * Simulates the AWS CloudWatch Logs API
 */
public class AWSCloudWatchFake {
    private final List<LogEntry> allLogs;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, String> credentials;
    
    public AWSCloudWatchFake(Map<String, String> credentials) {
        // Simulate AWS authentication
        validateAWSCredentials(credentials);
        this.credentials = credentials;
        
        // Load fake log data
        this.allLogs = loadLogsFromResource();
        System.out.println(">>> AWS CloudWatch Fake initialized with " + allLogs.size() + " log entries");
        System.out.println(">>> Connected to region: " + credentials.get("region"));
    }
    
    private void validateAWSCredentials(Map<String, String> credentials) {
        // Simulate AWS credential validation
        if (credentials.get("accessKeyId") == null || credentials.get("accessKeyId").isEmpty()) {
            throw new RuntimeException("AWS Error: Missing AWS_ACCESS_KEY_ID");
        }
        if (credentials.get("secretAccessKey") == null || credentials.get("secretAccessKey").isEmpty()) {
            throw new RuntimeException("AWS Error: Missing AWS_SECRET_ACCESS_KEY");
        }
        
        // In reality, AWS SDK would make a test API call here
        System.out.println(">>> AWS credentials validated successfully");
    }
    
    /**
     * Simulates the FilterLogEvents API call
     */
    public List<LogEntry> filterLogEvents(String logGroup, String filterPattern, int limit) {
        System.out.printf(">>> AWS API: FilterLogEvents(logGroup=%s, filter=%s, limit=%d)%n", 
            logGroup, filterPattern, limit);
        
        return allLogs.stream()
            .filter(log -> matchesLogGroup(log, logGroup))
            .filter(log -> matchesFilter(log, filterPattern))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private boolean matchesLogGroup(LogEntry log, String logGroup) {
        // Simple matching - in real AWS, this would be more complex
        return log.source().contains(logGroup.replace("/aws/", ""));
    }
    
    private boolean matchesFilter(LogEntry log, String filterPattern) {
        if (filterPattern == null || filterPattern.isEmpty()) {
            return true;
        }
        // Simple filter matching
        return log.message().contains(filterPattern) || 
               log.severity().equalsIgnoreCase(filterPattern);
    }
    
    private List<LogEntry> loadLogsFromResource() {
        List<LogEntry> logs = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/aws/cloudwatch-logs.json")) {
            JsonNode root = mapper.readTree(is);
            JsonNode events = root.get("events");
            
            for (JsonNode event : events) {
                long timestamp = event.get("timestamp").asLong();
                String message = event.get("message").asText();
                String logStream = event.get("logStreamName").asText();
                
                // Extract severity from message
                String severity = "INFO";
                if (message.contains("ERROR")) severity = "ERROR";
                else if (message.contains("WARN")) severity = "WARN";
                
                logs.add(new LogEntry(
                    Instant.ofEpochMilli(timestamp).toString(),
                    message,
                    severity,
                    logStream
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AWS logs from resource", e);
        }
        return logs;
    }
}
```

4. Update WatchTowerAgentTest.java to enable the real tests:
```java
package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for WatchTower.AI Agent
 * Branch 01-aws-mvp: Testing AWS-only troubleshooting implementation
 */
class WatchTowerAgentTest {
    
    protected WatchTowerAgent agent;
    
    @BeforeEach
    void setup() {
        agent = new WatchTowerAgent();
    }
    
    @Test
    @DisplayName("Troubleshoot: Why are payment APIs failing?")
    void troubleshootPaymentFailures() {
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println(">>> TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("timeout")
            .contains("connection pool")
            .contains("Recommendation");
    }
}
```

5. Add more realistic AWS log entries to src/test/resources/aws/cloudwatch-logs.json:
```json
{
  "events": [
    {
      "timestamp": 1705752000000,
      "message": "ERROR PaymentService - API timeout after 30000ms - Transaction ID: tx-12345",
      "logStreamName": "payment-service-prod"
    },
    {
      "timestamp": 1705752060000,
      "message": "ERROR PaymentService - Database connection pool exhausted. Active: 100, Idle: 0",
      "logStreamName": "payment-service-prod"
    },
    {
      "timestamp": 1705752120000,
      "message": "WARN PaymentService - High latency detected: p99=5000ms, p95=3000ms",
      "logStreamName": "payment-service-prod"
    },
    {
      "timestamp": 1705752180000,
      "message": "INFO UserService - Successfully processed 1523 requests",
      "logStreamName": "user-service-prod"
    },
    {
      "timestamp": 1705752240000,
      "message": "INFO MetricsCollector - Current request rate: 5000 req/min from IP 192.168.1.100",
      "logStreamName": "production-metrics"
    },
    {
      "timestamp": 1705752300000,
      "message": "ERROR PaymentService - Circuit breaker OPEN for payment-gateway",
      "logStreamName": "payment-service-prod"
    },
    {
      "timestamp": 1705752360000,
      "message": "INFO LoadBalancer - Rerouting traffic due to unhealthy instances in us-east-1a",
      "logStreamName": "infrastructure-prod"
    },
    {
      "timestamp": 1705752420000,
      "message": "WARN DatabaseConnection - Connection pool usage at 95%",
      "logStreamName": "database-prod"
    },
    {
      "timestamp": 1705752480000,
      "message": "INFO APIGateway - Request count: 45000, Error rate: 2.3%, Avg latency: 230ms",
      "logStreamName": "production-metrics"
    },
    {
      "timestamp": 1705752540000,
      "message": "ERROR PaymentService - Failed to connect to payment processor: Connection refused",
      "logStreamName": "payment-service-prod"
    }
  ]
}
```

6. Create a demo script demo.sh in the root:
```bash
#!/bin/bash
echo ">>> WatchTower.AI Demo - Branch: $(git branch --show-current)"
echo "================================"
echo ""
echo "Running troubleshooting analysis..."
echo ""

# Run the single test with clean output
mvn test -Dtest=WatchTowerAgentTest#troubleshootPaymentFailures -q

echo ""
echo ">>> Notice: Everything is hardcoded to AWS CloudWatch"
echo "   - Direct AWS client usage"
echo "   - AWS-specific log groups (/aws/...)"
echo "   - No way to support other cloud providers"
echo "   - Only one analysis type available"
echo ""
```

Make demo.sh executable.

7. Create the markdown file for this phase in docs/01-aws-mvp.md:
```markdown
# Phase 1: AWS MVP Success

## What We Built

Direct AWS CloudWatch integration with error troubleshooting:
- **Single Feature**: Root cause analysis for errors
- **Single Cloud**: AWS CloudWatch only
- **Single Use Case**: Payment service errors

## Demo: See It In Action

Run: `./demo.sh`

Notice how we get intelligent analysis of AWS CloudWatch logs.

## The Implementation

```java
// Everything is hardcoded to AWS
private final AWSCloudWatchFake awsClient;

// Direct client usage - no abstraction
List<LogEntry> logs = awsClient.filterLogEvents(
    "/aws/payment-service",  // Hardcoded!
    "ERROR",
    1000
);

// Single Feature: Root cause analysis
public String troubleshootErrors(String userQuery)

// Authentication is the first thing!
Map<String, String> awsCredentials = new HashMap<>();
awsCredentials.put("accessKeyId", System.getenv("AWS_ACCESS_KEY_ID"));
awsCredentials.put("secretAccessKey", System.getenv("AWS_SECRET_ACCESS_KEY"));
awsCredentials.put("region", "us-east-1");
```

## What's Working

- ✓ Simple, focused implementation
- ✓ Proper AWS authentication pattern
- ✓ Great for AWS-only customers
- ✓ Solves the immediate pain point
- ✓ Fast time to market

## Customer Feedback

> "This is great! Can you also generate daily summaries?"
>
> "We love it! But we need GCP support too..."
>
> "Perfect for AWS, but what about our Azure workloads?"

## The Problems Starting to Surface

- ✗ Only troubleshooting - no other analysis types
- ✗ AWS credentials hardcoded in constructor
- ✗ Different clouds = different auth patterns
- ✗ Everything hardcoded to AWS
- ✗ No way for the agent to know what's available

---

**Next**: Our biggest customer threatens to leave without GCP support...
```

IMPORTANT:
- The code should clearly show AWS-specific implementation
- Comments should hint at the coming problems (what about GCP?)
- Tests should produce beautiful, demo-worthy output
- Everything should compile and tests should pass
- This markdown file is created fresh in this branch

After creating these files:
1. Run `./validate.sh` to ensure everything works
2. Run `./demo.sh` to see the demo output
3. Review `docs/01-aws-mvp.md` for talking points
4. Commit: `git add . && git commit -m "AWS MVP: Initial implementation with CloudWatch only"`