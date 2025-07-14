package com.watchtower.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Universal client for ANY CloudLogSource that follows the protocol
 * 
 * This single client works with AWS, GCP, Azure, or any future source!
 */
@RequiredArgsConstructor
public class CloudLogSourceProtocolClient {
    private final Process serverProcess;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final ObjectMapper json = new ObjectMapper();
    
    /**
     * Connect to a CloudLogSource server
     */
    public static CloudLogSourceProtocolClient connect(String command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(process.getOutputStream()), 
            true
        );
        
        return new CloudLogSourceProtocolClient(process, reader, writer);
    }
    
    /**
     * Initialize connection
     */
    public Map<String, Object> initialize() throws IOException {
        ProtocolRequest request = ProtocolRequest.builder()
            .method("initialize")
            .params(Map.of("version", "1.0"))
            .id(UUID.randomUUID().toString())
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
            .id(UUID.randomUUID().toString())
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
    public Object execute(String operation, Map<String, Object> arguments) throws IOException {
        ProtocolRequest request = ProtocolRequest.builder()
            .method("execute")
            .params(Map.of(
                "operation", operation,
                "arguments", arguments
            ))
            .id(UUID.randomUUID().toString())
            .build();
            
        ProtocolResponse response = sendRequest(request);
        if (response.getError() != null) {
            throw new RuntimeException("Execute failed: " + response.getError().getMessage());
        }
        
        return response.getResult();
    }
    
    private ProtocolResponse sendRequest(ProtocolRequest request) throws IOException {
        // Send request
        String requestJson = json.writeValueAsString(request);
        writer.println(requestJson);
        
        // Read response
        String responseJson = reader.readLine();
        return json.readValue(responseJson, ProtocolResponse.class);
    }
    
    public void close() {
        writer.close();
        try {
            reader.close();
            serverProcess.destroy();
        } catch (IOException e) {
            // Ignore
        }
    }
}