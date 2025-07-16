package com.watchtower.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Universal client for ANY CloudLogSource that implements the protocol
 * 
 * This ONE client replaces all transport-specific clients!
 */
public class CloudLogSourceProtocolClient implements Closeable {
    private final Process serverProcess;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final ObjectMapper json = new ObjectMapper();
    
    /**
     * Connect to a CloudLogSource server via stdio
     */
    public static CloudLogSourceProtocolClient connect(String command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(process.getOutputStream())
        );
        
        return new CloudLogSourceProtocolClient(process, reader, writer);
    }
    
    private CloudLogSourceProtocolClient(Process process, BufferedReader reader, BufferedWriter writer) {
        this.serverProcess = process;
        this.reader = reader;
        this.writer = writer;
    }
    
    /**
     * Initialize the connection
     */
    public Map<String, Object> initialize() throws IOException {
        ProtocolRequest request = ProtocolRequest.builder()
            .method("initialize")
            .params(Map.of(
                "protocol_version", "1.0",
                "client_info", Map.of(
                    "name", "CloudLogSourceProtocolClient",
                    "version", "1.0"
                )
            ))
            .id(generateId())
            .build();
            
        ProtocolResponse response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Initialize failed: " + response.getError().getMessage());
        }
        
        return (Map<String, Object>) response.getResult();
    }
    
    /**
     * Discover available operations
     */
    public Map<String, Object> discover() throws IOException {
        ProtocolRequest request = ProtocolRequest.builder()
            .method("discover")
            .params(Map.of())
            .id(generateId())
            .build();
            
        ProtocolResponse response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Discover failed: " + response.getError().getMessage());
        }
        
        return (Map<String, Object>) response.getResult();
    }
    
    /**
     * Execute an operation
     */
    public Map<String, Object> execute(String operation, Map<String, Object> arguments) throws IOException {
        ProtocolRequest request = ProtocolRequest.builder()
            .method("execute")
            .params(Map.of(
                "operation", operation,
                "arguments", arguments
            ))
            .id(generateId())
            .build();
            
        ProtocolResponse response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Execute failed: " + response.getError().getMessage());
        }
        
        return (Map<String, Object>) response.getResult();
    }
    
    private ProtocolResponse sendRequest(ProtocolRequest request) throws IOException {
        // Send request as single line JSON
        String requestJson = json.writeValueAsString(request);
        writer.write(requestJson);
        writer.newLine();
        writer.flush();
        
        // Read response as single line JSON
        String responseJson = reader.readLine();
        if (responseJson == null) {
            throw new IOException("Server closed connection");
        }
        
        return json.readValue(responseJson, ProtocolResponse.class);
    }
    
    private String generateId() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    public void close() throws IOException {
        writer.close();
        reader.close();
        serverProcess.destroy();
    }
}