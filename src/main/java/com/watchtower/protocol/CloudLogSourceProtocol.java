package com.watchtower.protocol;

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
    // This is a marker interface to document the protocol
    // Actual implementation uses JSON-RPC messages
}