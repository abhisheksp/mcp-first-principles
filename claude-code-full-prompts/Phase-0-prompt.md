Create a Maven project for a technical talk demonstrating the evolution from hardcoded cloud integrations to MCP (Model Context Protocol). This is the foundation that all git branches will build upon.

Project location: /Users/abhishek/workspace/projects/mcp-first-principles

Required structure:

1. Create pom.xml with:
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.watchtower</groupId>
    <artifactId>mcp-first-principles</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.0</junit.version>
        <testcontainers.version>1.19.3</testcontainers.version>
    </properties>
    
    Include dependencies for:
    - JUnit 5 (with junit-jupiter-engine)
    - TestContainers (core and localstack modules)
    - AWS SDK v2 (cloudwatch logs)
    - Google Cloud Logging
    - Azure Monitor (for later branches)
    - Jackson for JSON parsing
    - SLF4J with simple implementation
    - AssertJ for better assertions
    
    Configure maven-surefire-plugin to:
    - Show test method names during execution
    - Show stdout/stderr in console
    - Use single fork for clearer output
</project>
```

2. Create directory structure:
```
src/
├── main/java/com/watchtower/
│   ├── model/
│   │   └── LogEntry.java (record with timestamp, message, severity, source)
│   ├── fakes/
│   │   └── FakeDataLoader.java (utility to load JSON from resources)
│   └── llm/
│       └── LLMFake.java (see details below)
└── test/
    ├── java/com/watchtower/
    │   └── WatchTowerAgentTest.java (base test class - see below)
    └── resources/
        ├── aws/
        │   └── cloudwatch-logs.json
        ├── gcp/
        │   └── stackdriver-logs.json  
        ├── azure/
        │   └── monitor-logs.json
        └── llm/
            ├── troubleshoot-payment-errors.txt
            ├── summary-daily-activity.txt
            └── anomaly-traffic-spike.txt
```

3. LLMFake implementation:
```java
public class LLMFake {
    private static final String LLM_RESOURCE_PATH = "/llm/";
    
    public String complete(String promptType, String userQuery, String data) {
        // promptType will be "troubleshoot", "summary", or "anomaly"
        // For foundation/early branches, only "troubleshoot" will be used
        // Load appropriate response from resources based on promptType
        
        // Format the response to include:
        // - Analysis header
        // - Key findings
        // - Recommendations
        
        return formattedResponse;
    }
}
```

4. WatchTowerAgentTest base structure:
```java
package com.watchtower;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base test class that will be used across all branches.
 * For the foundation, this is just a placeholder that compiles but doesn't run.
 * Each branch will implement WatchTowerAgent and these tests will start working.
 */
class WatchTowerAgentTest {
    
    // Commented out for foundation - will be uncommented in branch 01-aws-mvp
    // protected WatchTowerAgent agent;
    
    @BeforeEach
    void setup() {
        // Will be implemented differently in each branch
        // agent = new WatchTowerAgent();
    }
    
    @Test
    @DisplayName("🔍 Troubleshoot: Why are payment APIs failing?")
    void troubleshootPaymentFailures() {
        // Placeholder for foundation - will be implemented in branches
        System.out.println("Test will be implemented when WatchTowerAgent exists");
        assertThat(true).isTrue(); // Dummy assertion so test passes
        
        /* This is what the test will look like in branches:
        String analysis = agent.troubleshootErrors(
            "Payment API returning 500 errors in last hour"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔍 TROUBLESHOOTING ANALYSIS");
        System.out.println("=".repeat(50));
        System.out.println(analysis);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(analysis)
            .contains("timeout")
            .contains("connection pool")
            .contains("Recommendation");
        */
    }
    
    @Test
    @DisplayName("📊 Summary: What happened in the last 24 hours?")
    void generateDailySummary() {
        // Placeholder for foundation
        System.out.println("Test will be implemented when WatchTowerAgent exists");
        assertThat(true).isTrue();
        
        /* This is what the test will look like:
        String summary = agent.generateSummary("24h", "production");
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 DAILY SUMMARY");
        System.out.println("=".repeat(50));
        System.out.println(summary);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(summary)
            .contains("requests processed")
            .contains("error rate")
            .contains("peak traffic");
        */
    }
    
    @Test
    @DisplayName("🚨 Anomaly: Is this traffic pattern normal?")
    void detectTrafficAnomalies() {
        // Placeholder for foundation
        System.out.println("Test will be implemented when WatchTowerAgent exists");
        assertThat(true).isTrue();
        
        /* This is what the test will look like:
        String anomalies = agent.detectAnomalies(
            "baseline: 1000 req/min",
            "current: 5000 req/min from single IP"
        );
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🚨 ANOMALY DETECTION");  
        System.out.println("=".repeat(50));
        System.out.println(anomalies);
        System.out.println("=".repeat(50) + "\n");
        
        assertThat(anomalies)
            .contains("unusual spike")
            .contains("5x normal")
            .contains("potential DDoS");
        */
    }
}
```

5. Test resource files with realistic data:

aws/cloudwatch-logs.json:
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
    }
  ]
}
```

llm/troubleshoot-payment-errors.txt:
```
## Payment API Failure Analysis

**Root Cause Identified:**
The payment API failures are occurring due to a cascading failure pattern:

1. **Database Connection Pool Exhaustion**
   - Current pool size: 100 connections (all active, 0 idle)
   - Queries are taking longer than usual, holding connections

2. **Resulting Timeouts**
   - API requests timing out after 30 seconds
   - Upstream services receiving 500 errors

**Recommendations:**
1. Immediate: Increase connection pool size to 200
2. Short-term: Implement circuit breaker pattern
3. Long-term: Investigate slow queries causing connection holds

**Similar Past Incidents:**
- 2023-12-15: Same pattern during Black Friday sale
- Resolution: Scaled database read replicas
```

6. Create .gitignore:
```
target/
*.class
*.iml
.idea/
.vscode/
*.log
.DS_Store
dependency-reduced-pom.xml
```

7. Create README.md:
```markdown
# MCP First Principles - WatchTower.AI Journey

A live coding demonstration showing the natural evolution from hardcoded cloud integrations to the Model Context Protocol (MCP).

## Talk Structure

This repository uses git branches to show the progression:

1. `01-aws-mvp` - Initial AWS-only implementation
2. `02-gcp-pressure` - Copy-paste GCP addition (the mess begins)
3. `03-extract-interface` - First refactoring to common interface
4. `04-azure-discovery` - Adding discovery capabilities
5. `05-multiple-personas` - Multiple analysis methods
6. `06-transport-chaos` - Transport multiplication problem
7. `07-protocol-emerges` - Our own protocol design
8. `08-mcp-adoption` - Cloud sources as MCP servers

## Running the Demo

Each branch has the same test structure:
\`\`\`bash
# Switch to any branch
git checkout 01-aws-mvp

# Run the demo
mvn test

# Or run specific test
mvn test -Dtest=WatchTowerAgentTest#troubleshootPaymentFailures
\`\`\`

## Key Concepts Demonstrated

- Why discovery matters for AI agents
- How transport concerns multiply complexity  
- Why protocols beat APIs for AI integration
- How MCP emerges from good engineering practices
```

8. Create a validate.sh script in the root:
```bash
#!/bin/bash
set -e

echo "🔍 Validating MCP First Principles Foundation..."

# Check Maven
if ! mvn --version > /dev/null 2>&1; then
    echo "❌ Maven not found"
    exit 1
fi

# Compile
echo "📦 Compiling..."
if ! mvn clean compile; then
    echo "❌ Compilation failed"
    exit 1
fi

# Run tests (they should pass even without WatchTowerAgent)
echo "🧪 Running tests..."
if ! mvn test; then
    echo "❌ Tests failed"
    exit 1
fi

# Check key files exist
files=(
    "src/main/java/com/watchtower/model/LogEntry.java"
    "src/main/java/com/watchtower/llm/LLMFake.java"
    "src/test/resources/aws/cloudwatch-logs.json"
    "src/test/resources/llm/troubleshoot-payment-errors.txt"
)

for file in "${files[@]}"; do
    if [[ ! -f "$file" ]]; then
        echo "❌ Missing required file: $file"
        exit 1
    fi
done

echo "✅ Foundation validation passed!"
echo ""
echo "Next steps:"
echo "1. git add ."
echo "2. git commit -m 'Foundation: Project setup with fakes and test structure'"
echo "3. git checkout -b 01-aws-mvp"
echo "4. Run next prompt for AWS MVP implementation"
```

Make validate.sh executable.

9. Create the introduction markdown in docs/ directory:

Create docs/00-introduction.md:
```markdown
# From APIs to MCP: A Live Refactoring Journey

## Our Story: WatchTower.AI

You're the lead engineer at a hot startup that just raised Series A funding for your innovative log monitoring service.

- **Launch**: AWS CloudWatch support only
- **Traction**: Customers love the AI-powered analysis
- **Problem**: Big customers want multi-cloud support

---

## The Journey We'll Take Today

1. **Start** with working AWS integration
2. **Feel the pain** when adding GCP support
3. **Refactor** naturally toward better abstractions
4. **Discover** we've essentially built MCP
5. **Understand** why protocols beat APIs for AI tools

---

## Why This Matters

- Every AI tool faces this integration problem
- MCP is emerging as the standard solution
- Understanding the "why" makes adoption obvious

---

## The Core Agent Loop

```
User Query → Agent → LLM decides what data needed
↓
Fetch from cloud sources
↓
LLM analyzes with context
↓
Return insights to user
```

**Key Insight**: The LLM needs to know what tools are available!
```

IMPORTANT:
- All test resources should contain realistic log data that tells a story
- The LLMFake should return responses that look like real analysis
- Keep imports minimal - only what's needed
- Ensure all code compiles and could theoretically run tests (even though WatchTowerAgent doesn't exist yet)
- Only create the introduction markdown - other markdown files come with their respective branches