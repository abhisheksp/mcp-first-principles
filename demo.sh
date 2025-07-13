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