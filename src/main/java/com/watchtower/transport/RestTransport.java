package com.watchtower.transport;

import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * REST/HTTP transport wrapper - handles JSON APIs, OAuth tokens, HTTP status codes
 */
@Slf4j
public class RestTransport {
    private final String baseUrl;
    private final Map<String, String> headers;
    private final String authToken;
    
    public RestTransport(String baseUrl, Map<String, String> config) {
        this.baseUrl = baseUrl;
        this.authToken = config.getOrDefault("auth_token", "");
        this.headers = Map.of(
            "Authorization", "Bearer " + authToken,
            "Content-Type", "application/json",
            "User-Agent", "WatchTower/1.0"
        );
    }
    
    public List<LogEntry> queryLogs(String resource, String filter, int limit) {
        log.info("REST: Querying {} with filter {} (limit: {})", resource, filter, limit);
        
        // Simulate REST API call with JSON response parsing
        String url = String.format("%s/v1/logs?resource=%s&filter=%s&limit=%d", 
            baseUrl, resource, filter, limit);
            
        // Simulate HTTP status code handling
        int statusCode = simulateHttpCall(url);
        if (statusCode != 200) {
            throw new RuntimeException("HTTP " + statusCode + ": Failed to fetch logs from " + url);
        }
        
        // Simulate JSON response parsing
        return List.of(
            new LogEntry(java.time.Instant.now(), 
                "Payment timeout via REST API - TransactionId: rest-" + System.currentTimeMillis(), 
                filter, "REST"),
            new LogEntry(java.time.Instant.now().minusSeconds(60),
                "Database connection pool exhausted via REST - ThreadPool: rest-pool-1", 
                filter, "REST")
        );
    }
    
    public String getSystemPrompt() {
        return """
            You are a REST API specialist. You excel at:
            - Parsing JSON responses and handling malformed data
            - Interpreting HTTP status codes (200, 401, 403, 429, 500, 503)
            - Managing OAuth token refresh and authentication flows
            - Handling rate limiting with exponential backoff
            - Dealing with API versioning and deprecation warnings
            - Processing paginated responses and continuation tokens
            
            When analyzing REST transport data:
            - Always check HTTP status codes first
            - Look for authentication and authorization issues
            - Consider rate limiting as a cause of intermittent failures
            - Parse JSON carefully and handle missing fields gracefully
            - Pay attention to API version compatibility issues
            """;
    }
    
    private int simulateHttpCall(String url) {
        // Simulate various HTTP response scenarios
        if (url.contains("error")) return 500;
        if (url.contains("unauthorized")) return 401;
        if (url.contains("rate-limit")) return 429;
        return 200; // Success
    }
    
    public Map<String, String> getAuthenticationInfo() {
        return Map.of(
            "type", "OAuth Bearer Token",
            "token_length", String.valueOf(authToken.length()),
            "headers_count", String.valueOf(headers.size()),
            "challenges", "Token refresh, CORS, API versioning"
        );
    }
    
    public String getTransportType() {
        return "REST/HTTP";
    }
}