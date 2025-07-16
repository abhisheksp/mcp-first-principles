package com.watchtower.protocol.servers;

import com.watchtower.protocol.*;
import com.watchtower.sources.AWSLogSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;

/**
 * AWS CloudLogSource exposed via the protocol
 */
public class AWSProtocolServerMain {
    public static void main(String[] args) throws Exception {
        // Create AWS source
        AWSLogSource awsSource = new AWSLogSource();
        CloudLogSourceProtocolServer server = new CloudLogSourceProtocolServer(awsSource, "AWS");
        
        // JSON mapper
        ObjectMapper json = new ObjectMapper();
        
        // Read from stdin, write to stdout
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        
        // Protocol loop
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                // Parse request
                ProtocolRequest request = json.readValue(line, ProtocolRequest.class);
                
                // Handle request
                ProtocolResponse response = server.handleRequest(request);
                
                // Send response
                writer.write(json.writeValueAsString(response));
                writer.newLine();
                writer.flush();
                
            } catch (Exception e) {
                // Protocol error
                ProtocolResponse error = ProtocolResponse.error(
                    null,
                    ProtocolError.PARSE_ERROR,
                    e.getMessage()
                );
                writer.write(json.writeValueAsString(error));
                writer.newLine();
                writer.flush();
            }
        }
    }
}