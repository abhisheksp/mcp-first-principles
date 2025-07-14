package com.watchtower.transport;

import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket transport wrapper - handles real-time streaming, connection state, message framing
 */
@Slf4j
public class WebSocketTransport {
    private final String wsUrl;
    private final Map<String, String> headers;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private Thread listenerThread;
    
    public WebSocketTransport(String wsUrl, Map<String, String> config) {
        this.wsUrl = wsUrl;
        this.headers = Map.of(
            "Authorization", "Bearer " + config.getOrDefault("ws_token", ""),
            "Sec-WebSocket-Protocol", "logs-v1",
            "Origin", config.getOrDefault("origin", "https://watchtower.ai")
        );
    }
    
    public void connect() {
        log.info("WebSocket: Connecting to {}", wsUrl);
        
        // Simulate WebSocket handshake
        simulateHandshake();
        connected.set(true);
        
        // Start message listener thread
        startMessageListener();
        
        log.info("WebSocket: Connected successfully");
    }
    
    public List<LogEntry> subscribeLogs(String resource, String filter) {
        if (!connected.get()) {
            throw new RuntimeException("WebSocket not connected. Call connect() first.");
        }
        
        // Send subscription message
        String subscribeMessage = String.format(
            "{\"action\":\"subscribe\",\"resource\":\"%s\",\"filter\":\"%s\"}", 
            resource, filter);
        
        sendMessage(subscribeMessage);
        log.info("WebSocket: Subscribed to {} with filter {}", resource, filter);
        
        // Simulate receiving real-time log messages
        return List.of(
            new LogEntry(java.time.Instant.now(),
                "Real-time payment alert via WebSocket - TransactionId: ws-" + System.currentTimeMillis(),
                filter, "WebSocket"),
            new LogEntry(java.time.Instant.now().minusSeconds(5),
                "WebSocket connection heartbeat: ping=12ms, queue_size=3",
                filter, "WebSocket")
        );
    }
    
    private void simulateHandshake() {
        try {
            Thread.sleep(100); // Simulate handshake delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WebSocket handshake interrupted", e);
        }
    }
    
    private void startMessageListener() {
        listenerThread = new Thread(() -> {
            while (connected.get()) {
                try {
                    // Simulate receiving messages
                    String message = generateIncomingMessage();
                    messageQueue.offer(message);
                    Thread.sleep(1000); // Simulate message frequency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        listenerThread.setName("WebSocket-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    private String generateIncomingMessage() {
        return String.format(
            "{\"timestamp\":\"%s\",\"level\":\"INFO\",\"message\":\"Live system metrics update\",\"source\":\"monitoring\"}",
            java.time.Instant.now());
    }
    
    private void sendMessage(String message) {
        log.debug("WebSocket: Sending message: {}", message);
        // Simulate message sending
    }
    
    public void disconnect() {
        log.info("WebSocket: Disconnecting from {}", wsUrl);
        connected.set(false);
        
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        
        messageQueue.clear();
        log.info("WebSocket: Disconnected");
    }
    
    public String getSystemPrompt() {
        return """
            You are a WebSocket specialist. You excel at:
            - Handling real-time streaming data and message framing
            - Managing connection state, reconnection logic, and heartbeats
            - Interpreting WebSocket close codes and error conditions
            - Dealing with message ordering, delivery guarantees, and backpressure
            - Handling subprotocol negotiation and extension management
            - Processing high-frequency message streams with low latency
            
            When analyzing WebSocket transport data:
            - Always check connection state and close codes first
            - Look for message ordering and delivery issues
            - Consider backpressure and queue overflow scenarios
            - Monitor connection stability and reconnection patterns
            - Pay attention to message framing and protocol compliance
            - Handle partial messages and connection interruptions gracefully
            """;
    }
    
    public Map<String, String> getConnectionInfo() {
        return Map.of(
            "ws_url", wsUrl,
            "connected", String.valueOf(connected.get()),
            "message_queue_size", String.valueOf(messageQueue.size()),
            "headers_count", String.valueOf(headers.size()),
            "challenges", "Connection state, Message framing, Real-time streaming, Reconnection"
        );
    }
    
    public String getTransportType() {
        return "WebSocket/Real-time";
    }
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public int getQueueSize() {
        return messageQueue.size();
    }
}