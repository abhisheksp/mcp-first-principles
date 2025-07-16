package com.watchtower.protocol;

/**
 * CloudLogSource Protocol Specification v1.0
 * 
 * This protocol standardizes how clients communicate with CloudLogSource servers.
 * ALL implementations MUST follow these rules exactly.
 * 
 * TRANSPORT RULES:
 * - Communication MUST use stdio (stdin for requests, stdout for responses)
 * - Servers MUST read from stdin and write to stdout
 * - Each message MUST be a complete JSON object on a single line
 * - Each message MUST be terminated with a newline character (\n)
 * - Servers MUST NOT write anything else to stdout
 * 
 * MESSAGE FORMAT:
 * - All messages MUST follow JSON-RPC 2.0 specification
 * - Request format:
 *   {
 *     "jsonrpc": "2.0",
 *     "method": "methodName",
 *     "params": { ... },
 *     "id": "unique-id"
 *   }
 * 
 * - Success response format:
 *   {
 *     "jsonrpc": "2.0",
 *     "result": { ... },
 *     "id": "matching-request-id"
 *   }
 * 
 * - Error response format:
 *   {
 *     "jsonrpc": "2.0",
 *     "error": {
 *       "code": -32601,
 *       "message": "Method not found",
 *       "data": { ... }  // optional
 *     },
 *     "id": "matching-request-id"
 *   }
 * 
 * REQUIRED METHODS:
 * 
 * 1. "initialize"
 *    Purpose: Establish connection and exchange capabilities
 *    Params: {
 *      "protocol_version": "1.0",
 *      "client_info": { "name": "...", "version": "..." }
 *    }
 *    Result: {
 *      "protocol_version": "1.0",
 *      "server_info": { "name": "...", "version": "...", "provider": "..." },
 *      "capabilities": { ... }
 *    }
 * 
 * 2. "discover"
 *    Purpose: List available operations
 *    Params: {}
 *    Result: {
 *      "operations": [
 *        {
 *          "name": "fetchLogs",
 *          "description": "...",
 *          "parameters": { ... }
 *        },
 *        ...
 *      ]
 *    }
 * 
 * 3. "execute"
 *    Purpose: Execute a specific operation
 *    Params: {
 *      "operation": "operationName",
 *      "arguments": { ... }
 *    }
 *    Result: (operation-specific)
 * 
 * ERROR CODES:
 * - -32700: Parse error (invalid JSON)
 * - -32600: Invalid request (missing required fields)
 * - -32601: Method not found
 * - -32602: Invalid params
 * - -32603: Internal error
 * 
 * LIFECYCLE:
 * 1. Client sends "initialize" request
 * 2. Server responds with capabilities
 * 3. Client sends "discover" to learn operations
 * 4. Client sends "execute" for specific operations
 * 5. Either party closes stdin to end session
 * 
 * EXAMPLE SESSION:
 * C: {"jsonrpc":"2.0","method":"initialize","params":{"protocol_version":"1.0"},"id":"1"}
 * S: {"jsonrpc":"2.0","result":{"protocol_version":"1.0","server_info":{...}},"id":"1"}
 * C: {"jsonrpc":"2.0","method":"discover","params":{},"id":"2"}
 * S: {"jsonrpc":"2.0","result":{"operations":[...]},"id":"2"}
 * C: {"jsonrpc":"2.0","method":"execute","params":{"operation":"fetchLogs",...},"id":"3"}
 * S: {"jsonrpc":"2.0","result":{...},"id":"3"}
 */
public interface CloudLogSourceProtocol {
    /**
     * Handle a protocol request and return a protocol response.
     * Implementations MUST support all required methods.
     */
    ProtocolResponse handleRequest(ProtocolRequest request);
}