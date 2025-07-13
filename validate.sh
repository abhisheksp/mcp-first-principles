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