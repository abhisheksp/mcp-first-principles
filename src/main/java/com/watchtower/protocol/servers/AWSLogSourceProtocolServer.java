package com.watchtower.protocol.servers;

import com.watchtower.protocol.*;
import com.watchtower.sources.AWSLogSource;
import com.watchtower.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * AWS Log Source exposed via CloudLogSource Protocol
 * 
 * This replaces ALL the transport-specific implementations!
 */
public class AWSLogSourceProtocolServer implements CloudLogSourceProtocol {
    private final AWSLogSource logSource;
    private final ObjectMapper json = new ObjectMapper();
    private boolean initialized = false;
    
    public AWSLogSourceProtocolServer() {
        this.logSource = new AWSLogSource();
    }
    
    @Override
    public ProtocolResponse handleRequest(ProtocolRequest request) {
        try {
            return switch (request.getMethod()) {
                case "initialize" -> handleInitialize(request);
                case "discover" -> handleDiscover(request);
                case "execute" -> handleExecute(request);
                default -> ProtocolResponse.error(
                    request.getId(),
                    ProtocolError.METHOD_NOT_FOUND,
                    "Unknown method: " + request.getMethod()
                );
            };
        } catch (Exception e) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INTERNAL_ERROR,
                e.getMessage()
            );
        }
    }
    
    private ProtocolResponse handleInitialize(ProtocolRequest request) {
        // Initialize the AWS source
        Map<String, String> config = Map.of(
            "AWS_ACCESS_KEY_ID", getConfigValue("AWS_ACCESS_KEY_ID"),
            "AWS_SECRET_ACCESS_KEY", getConfigValue("AWS_SECRET_ACCESS_KEY")
        );
        logSource.initialize(config);
        initialized = true;
        
        return ProtocolResponse.success(request.getId(), Map.of(
            "version", "1.0",
            "serverInfo", Map.of(
                "name", "AWS CloudWatch Logs",
                "provider", "AWS"
            ),
            "capabilities", logSource.getCapabilities()
        ));
    }
    
    private ProtocolResponse handleDiscover(ProtocolRequest request) {
        if (!initialized) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INVALID_REQUEST,
                "Not initialized"
            );
        }
        
        var operations = List.of(
            Map.of(
                "name", "fetchLogs",
                "description", "Fetch logs from CloudWatch",
                "params", Map.of(
                    "resource", "string",
                    "filter", "string", 
                    "limit", "number"
                )
            ),
            Map.of(
                "name", "getCapabilities",
                "description", "Get source capabilities",
                "params", Map.of()
            )
        );
        
        return ProtocolResponse.success(request.getId(), Map.of(
            "operations", operations
        ));
    }
    
    private ProtocolResponse handleExecute(ProtocolRequest request) {
        if (!initialized) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INVALID_REQUEST,
                "Not initialized"
            );
        }
        
        Map<String, Object> params = request.getParams();
        String operation = (String) params.get("operation");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        return switch (operation) {
            case "fetchLogs" -> {
                String resource = (String) arguments.get("resource");
                String filter = (String) arguments.get("filter");
                int limit = (Integer) arguments.get("limit");
                
                List<LogEntry> logs = logSource.fetchLogs(resource, filter, limit);
                yield ProtocolResponse.success(request.getId(), Map.of(
                    "logs", logs,
                    "count", logs.size()
                ));
            }
            case "getCapabilities" -> {
                yield ProtocolResponse.success(
                    request.getId(), 
                    logSource.getCapabilities()
                );
            }
            default -> ProtocolResponse.error(
                request.getId(),
                ProtocolError.INVALID_PARAMS,
                "Unknown operation: " + operation
            );
        };
    }
    
    private String getConfigValue(String key) {
        // Try system property first (for tests), then environment variable
        String value = System.getProperty(key);
        if (value != null) return value;
        
        value = System.getenv(key);
        return value != null ? value : ""; // Return empty string instead of null
    }
}