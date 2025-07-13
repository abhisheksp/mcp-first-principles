package com.watchtower.sources;

import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.util.Map;

/**
 * Describes what a CloudLogSource can do.
 * This enables dynamic discovery of source capabilities.
 * Note: Capabilities are operation-focused, not persona-aware.
 */
@Value
@Builder
public class SourceCapabilities {
    String provider;
    String description;
    
    // What operations does this source support?
    List<String> supportedOperations;  // e.g., fetchLogs, fetchMetrics, streamLogs
    
    // What log resources are available?
    List<String> supportedResources;  // e.g., payment-service, user-service
    
    // What filters can be used?
    List<String> supportedFilters;  // e.g., ERROR, WARN, INFO
    
    // What time ranges are supported?
    List<String> supportedTimeRanges;  // e.g., 1h, 24h, 7d, 30d
    
    // Any provider-specific metadata
    Map<String, String> metadata;
}