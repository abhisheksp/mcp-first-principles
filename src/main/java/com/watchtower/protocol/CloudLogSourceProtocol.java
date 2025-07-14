package com.watchtower.protocol;

import java.util.Map;

/**
 * CloudLogSource Protocol Specification v1.0
 * 
 * This protocol defines how clients communicate with CloudLogSource servers.
 * ALL implementations MUST follow these rules exactly.
 * 
 * TRANSPORT RULES:
 * - Communication MUST use stdio (stdin for requests, stdout for responses)
 * - Each message MUST be a complete JSON object on a single line
 * - Each message MUST be terminated with a newline character (\n)
 * - Servers MUST NOT write anything else to stdout (errors go to stderr)
 * 
 * MESSAGE FORMAT:
 * - All messages MUST follow JSON-RPC 2.0 specification
 * - Requests: {"jsonrpc": "2.0", "method": "...", "params": {...}, "id": "..."}
 * - Success: {"jsonrpc": "2.0", "result": {...}, "id": "..."}
 * - Error: {"jsonrpc": "2.0", "error": {"code": ..., "message": "..."}, "id": "..."}
 * 
 * REQUIRED METHODS:
 * 1. "initialize" - Establish connection and capabilities
 *    params: {"version": "1.0", "clientInfo": {...}}
 *    result: {"version": "1.0", "serverInfo": {...}, "capabilities": {...}}
 * 
 * 2. "discover" - List available operations
 *    params: {}
 *    result: {"operations": [{"name": "...", "description": "...", "params": {...}}]}
 * 
 * 3. "execute" - Perform an operation
 *    params: {"operation": "...", "arguments": {...}}
 *    result: (operation-specific)
 * 
 * ERROR CODES (from JSON-RPC spec):
 * - -32700: Parse error (invalid JSON)
 * - -32600: Invalid request (not valid JSON-RPC)
 * - -32601: Method not found
 * - -32602: Invalid params
 * - -32603: Internal error
 * - -32000 to -32099: Server-defined errors
 * 
 * LIFECYCLE:
 * 1. Server starts and waits for stdin
 * 2. Client sends "initialize" request
 * 3. Server responds with capabilities
 * 4. Client sends "discover" to learn operations
 * 5. Client sends "execute" for specific operations
 * 6. Either party can close stdin to terminate
 * 
 * EXAMPLE SESSION:
 * → {"jsonrpc": "2.0", "method": "initialize", "params": {"version": "1.0"}, "id": "1"}
 * ← {"jsonrpc": "2.0", "result": {"version": "1.0", "serverInfo": {"name": "AWS"}}, "id": "1"}
 * → {"jsonrpc": "2.0", "method": "discover", "params": {}, "id": "2"}
 * ← {"jsonrpc": "2.0", "result": {"operations": [{"name": "fetchLogs", ...}]}, "id": "2"}
 */
public interface CloudLogSourceProtocol {
    
    /**
     * Process a JSON-RPC request and return a response.
     * Implementations MUST handle all required methods.
     * 
     * @param request The JSON-RPC request
     * @return The JSON-RPC response
     */
    ProtocolResponse handleRequest(ProtocolRequest request);
}