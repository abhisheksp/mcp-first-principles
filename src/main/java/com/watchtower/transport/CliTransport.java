package com.watchtower.transport;

import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.Arrays;

/**
 * CLI transport wrapper - handles command execution, output parsing, exit codes
 */
@Slf4j
public class CliTransport {
    private final String cliPath;
    private final Map<String, String> environment;
    private final String configPath;
    
    public CliTransport(String cliPath, Map<String, String> config) {
        this.cliPath = cliPath;
        this.configPath = config.getOrDefault("config_path", "~/.config/cli");
        this.environment = Map.of(
            "CLI_CONFIG", configPath,
            "CLI_PROFILE", config.getOrDefault("profile", "default"),
            "CLI_OUTPUT", "json",
            "CLI_TIMEOUT", "30"
        );
    }
    
    public List<LogEntry> executeLogs(String resource, String filter, int limit) {
        String command = buildLogCommand(resource, filter, limit);
        log.info("CLI: Executing command: {}", command);
        
        try {
            CliResult result = executeCommand(command);
            
            if (result.exitCode != 0) {
                throw new RuntimeException(String.format(
                    "CLI command failed with exit code %d: %s", 
                    result.exitCode, result.stderr));
            }
            
            return parseCliOutput(result.stdout, filter);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute CLI command: " + command, e);
        }
    }
    
    private String buildLogCommand(String resource, String filter, int limit) {
        return String.format("%s logs query --resource=%s --filter=%s --limit=%d --output=json", 
            cliPath, resource, filter, limit);
    }
    
    private CliResult executeCommand(String command) throws IOException {
        // Simulate CLI execution
        log.debug("Simulating CLI execution: {}", command);
        
        // Simulate different exit codes based on command
        int exitCode = 0;
        String stdout = generateCliOutput();
        String stderr = "";
        
        if (command.contains("invalid")) {
            exitCode = 1;
            stderr = "Error: Invalid resource specified";
        } else if (command.contains("timeout")) {
            exitCode = 124;
            stderr = "Error: Command timed out after 30 seconds";
        }
        
        return new CliResult(exitCode, stdout, stderr);
    }
    
    private String generateCliOutput() {
        return """
            {
              "logs": [
                {
                  "timestamp": "2024-01-20T10:00:00Z",
                  "level": "ERROR",
                  "message": "Payment processor timeout via CLI - TransactionId: cli-tx-789",
                  "source": "payment-service"
                },
                {
                  "timestamp": "2024-01-20T09:59:00Z",
                  "level": "WARN",
                  "message": "CLI authentication token expires in 5 minutes",
                  "source": "auth-service"
                }
              ],
              "total": 156,
              "truncated": true
            }
            """;
    }
    
    private List<LogEntry> parseCliOutput(String output, String filter) {
        // Simulate JSON parsing of CLI output
        return List.of(
            new LogEntry(java.time.Instant.now(),
                "Payment processor timeout via CLI - TransactionId: cli-tx-789",
                filter, "CLI"),
            new LogEntry(java.time.Instant.now().minusSeconds(60),
                "CLI authentication token expires in 5 minutes",
                filter, "CLI")
        );
    }
    
    public String getSystemPrompt() {
        return """
            You are a CLI specialist. You excel at:
            - Parsing command-line output in various formats (JSON, CSV, plain text)
            - Interpreting exit codes and error messages from shell commands
            - Managing authentication via config files, environment variables, and tokens
            - Handling timeouts, interruptions, and process management
            - Dealing with cross-platform compatibility issues
            - Processing structured and unstructured command output
            
            When analyzing CLI transport data:
            - Always check exit codes first (0=success, 1=general error, 124=timeout, etc.)
            - Look for authentication and configuration issues in stderr
            - Consider PATH and environment variable problems
            - Parse stdout carefully handling different output formats
            - Pay attention to version compatibility and deprecated flags
            - Handle partial output from interrupted commands gracefully
            """;
    }
    
    public Map<String, String> getExecutionInfo() {
        return Map.of(
            "cli_path", cliPath,
            "config_path", configPath,
            "environment_vars", String.valueOf(environment.size()),
            "challenges", "Exit codes, Output parsing, Environment setup, Process management"
        );
    }
    
    public String getTransportType() {
        return "CLI/Process";
    }
    
    private record CliResult(int exitCode, String stdout, String stderr) {}
}