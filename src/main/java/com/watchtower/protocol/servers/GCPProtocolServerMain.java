package com.watchtower.protocol.servers;

import com.watchtower.protocol.*;
import com.watchtower.sources.GCPLogSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;

/**
 * GCP CloudLogSource exposed via the protocol
 */
public class GCPProtocolServerMain {
    public static void main(String[] args) throws Exception {
        // Create GCP source
        GCPLogSource gcpSource = new GCPLogSource();
        CloudLogSourceProtocolServer server = new CloudLogSourceProtocolServer(gcpSource, "GCP");
        
        // Same protocol handling as AWS!
        ObjectMapper json = new ObjectMapper();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                ProtocolRequest request = json.readValue(line, ProtocolRequest.class);
                ProtocolResponse response = server.handleRequest(request);
                
                writer.write(json.writeValueAsString(response));
                writer.newLine();
                writer.flush();
                
            } catch (Exception e) {
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