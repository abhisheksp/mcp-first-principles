# MCP First Principles - WatchTower.AI Journey

A live coding demonstration showing the natural evolution from hardcoded cloud integrations to the Model Context Protocol (MCP).

## Quick Start - Phase 6 Protocol Servers

We've configured Maven with custom targets for easy MCP server startup:

```bash
# Terminal 1: Start AWS MCP Server (port 8001)
mvn exec:java@aws

# Terminal 2: Start GCP MCP Server (port 8002)
mvn exec:java@gcp

# Terminal 3: Run the multi-source protocol tests
mvn test -Dtest=WatchTowerAgentProtocolTest
```

## Talk Structure

This repository demonstrates the progression through phases:

1. `01-aws-mvp` - Initial AWS-only implementation
2. `02-gcp-pressure` - Copy-paste GCP addition (the mess begins)
3. `03-extract-interface` - First refactoring to common interface
4. `04-function-calling` - LLM orchestration with functions
5. `05-transport-chaos` - Transport multiplication problem
6. `06-protocol-revelation` - MCP emerges as the solution!

## Running the Demo

For Phase 6 (Protocol):
```bash
# Start both MCP servers (in separate terminals)
mvn exec:java@aws
mvn exec:java@gcp

# Run the protocol tests
mvn test -Dtest=WatchTowerAgentProtocolTest

# Or run specific test scenarios
mvn test -Dtest=WatchTowerAgentProtocolTest#testMultiSourceProtocol
mvn test -Dtest=WatchTowerAgentProtocolTest#testUnifiedTroubleshooting
```

## Key Concepts Demonstrated

- Why discovery matters for AI agents
- How transport concerns multiply complexity  
- Why protocols beat APIs for AI integration
- How MCP emerges from good engineering practices