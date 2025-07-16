package com.watchtower.protocol;

import com.watchtower.sources.CloudLogSource;
import com.watchtower.model.*;
import java.util.*;

/**
 * Protocol server that wraps ANY CloudLogSource
 * 
 * This single implementation replaces all transport-specific wrappers!
 */
public class CloudLogSourceProtocolServer implements CloudLogSourceProtocol {
    private final CloudLogSource source;
    private final String sourceName;
    private boolean initialized = false;
    
    public CloudLogSourceProtocolServer(CloudLogSource source, String sourceName) {
        this.source = source;
        this.sourceName = sourceName;
    }
    
    @Override
    public ProtocolResponse handleRequest(ProtocolRequest request) {
        // Validate request
        if (request.getMethod() == null || request.getId() == null) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INVALID_REQUEST,
                "Missing required fields"
            );
        }
        
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
        Map<String, Object> params = request.getParams();
        String protocolVersion = (String) params.get("protocol_version");
        
        if (!"1.0".equals(protocolVersion)) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INVALID_PARAMS,
                "Unsupported protocol version"
            );
        }
        
        // Initialize the source with environment credentials
        Map<String, String> config = new HashMap<>();
        if ("AWS".equals(sourceName)) {
            config.put("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"));
            config.put("AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY"));
        } else if ("GCP".equals(sourceName)) {
            config.put("GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        }
        
        source.initialize(config);
        initialized = true;
        
        // Return server info and capabilities
        return ProtocolResponse.success(request.getId(), Map.of(
            "protocol_version", "1.0",
            "server_info", Map.of(
                "name", sourceName + " CloudLogSource",
                "version", "1.0",
                "provider", source.getCloudProvider()
            ),
            "capabilities", Map.of(
                "supports_logs", true,
                "supports_metrics", true,
                "max_logs_per_request", 1000
            )
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
        
        // Describe available operations
        List<Map<String, Object>> operations = List.of(
            Map.of(
                "name", "fetchLogs",
                "description", "Fetch logs from " + sourceName,
                "parameters", Map.of(
                    "resource", Map.of("type", "string", "description", "Service name"),
                    "filter", Map.of("type", "string", "description", "Log level filter"),
                    "limit", Map.of("type", "integer", "description", "Maximum logs")
                )
            ),
            Map.of(
                "name", "fetchMetrics",
                "description", "Fetch metrics from " + sourceName,
                "parameters", Map.of(
                    "resource", Map.of("type", "string", "description", "Service name"),
                    "metricName", Map.of("type", "string", "description", "Metric to fetch"),
                    "timeRange", Map.of("type", "string", "description", "Time range")
                )
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
        
        if (operation == null || arguments == null) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INVALID_PARAMS,
                "Missing operation or arguments"
            );
        }
        
        try {
            Object result = switch (operation) {
                case "fetchLogs" -> {
                    String resource = (String) arguments.get("resource");
                    String filter = (String) arguments.get("filter");
                    Integer limit = (Integer) arguments.get("limit");
                    
                    List<LogEntry> logs = source.fetchLogs(resource, filter, limit);
                    yield Map.of(
                        "logs", logs,
                        "count", logs.size(),
                        "source", sourceName
                    );
                }
                
                case "fetchMetrics" -> {
                    String resource = (String) arguments.get("resource");
                    String metricName = (String) arguments.get("metricName");
                    String timeRange = (String) arguments.get("timeRange");
                    
                    List<Metric> metrics = source.fetchMetrics(resource, metricName, timeRange);
                    yield Map.of(
                        "metrics", metrics,
                        "count", metrics.size(),
                        "source", sourceName
                    );
                }
                
                default -> throw new IllegalArgumentException("Unknown operation: " + operation);
            };
            
            return ProtocolResponse.success(request.getId(), result);
            
        } catch (Exception e) {
            return ProtocolResponse.error(
                request.getId(),
                ProtocolError.INTERNAL_ERROR,
                "Operation failed: " + e.getMessage()
            );
        }
    }
}