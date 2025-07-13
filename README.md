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
```bash
# Switch to any branch
git checkout 01-aws-mvp

# Run the demo
mvn test

# Or run specific test
mvn test -Dtest=WatchTowerAgentTest#troubleshootPaymentFailures
```

## Key Concepts Demonstrated

- Why discovery matters for AI agents
- How transport concerns multiply complexity  
- Why protocols beat APIs for AI integration
- How MCP emerges from good engineering practices