package com.watchtower.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * CloudLogSource Protocol Specification v1.0
 * 
 * This protocol defines a standard way for agents to communicate with
 * cloud log sources, regardless of the underlying implementation.
 * 
 * TRANSPORT RULES:
 * - For this demo: TCP sockets (MCP standard uses stdio)
 * - Each message MUST be a JSON object on a single line
 * - Each message MUST end with newline (\n)
 * 
 * MESSAGE FORMAT:
 * - All messages MUST follow JSON-RPC 2.0 specification
 * - Request: {"jsonrpc":"2.0","method":"...","params":{...},"id":"..."}
 * - Response: {"jsonrpc":"2.0","result":{...},"id":"..."} 
 * - Error: {"jsonrpc":"2.0","error":{"code":...,"message":"..."},"id":"..."}
 * 
 * LIFECYCLE:
 * 1. initialize - Establish connection and authenticate
 * 2. discover - List available functions
 * 3. execute - Call a function
 * 4. close - Graceful shutdown
 * 
 * ERROR CODES:
 * - -32700: Parse error
 * - -32600: Invalid request
 * - -32601: Method not found
 * - -32602: Invalid params
 * - -32603: Internal error
 */
public interface CloudLogSourceProtocol {
    
    /**
     * Initialize the server with the provided credentials/configuration.
     * This is the first method called after connection establishment.
     * 
     * @param request The initialize request containing credentials
     * @return JsonNode result indicating successful initialization
     */
    JsonNode initialize(ProtocolMessages.Request request);
    
    /**
     * Discover available tools/functions that this server provides.
     * This allows clients to understand what capabilities are available.
     * 
     * @return JsonNode containing the list of available functions
     */
    JsonNode discover();
    
    /**
     * Execute a specific function/tool with the provided parameters.
     * This is how clients invoke the server's capabilities.
     * 
     * @param request The execute request containing function name and parameters
     * @return JsonNode result of the function execution
     */
    JsonNode execute(ProtocolMessages.Request request);
}