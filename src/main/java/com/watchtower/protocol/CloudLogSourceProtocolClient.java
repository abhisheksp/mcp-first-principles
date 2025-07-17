package com.watchtower.protocol;

import com.watchtower.functions.FunctionDefinition;
import java.util.List;
import java.util.Map;

/**
 * CloudLogSource Protocol Client Interface
 * 
 * This interface defines the core methods that every protocol client should implement
 * to interact with CloudLogSource protocol servers through the standardized protocol.
 * 
 * This represents the "client side" of the protocol - what agents/LLMs use
 * to discover and execute functions from protocol servers.
 */
public interface CloudLogSourceProtocolClient {
    
    /**
     * Initialize the connection with the MCP server.
     * This establishes the connection and authenticates with provided credentials.
     * 
     * @param credentials Configuration and authentication data
     */
    void initialize(Map<String, String> credentials);
    
    /**
     * Discover available functions/tools from the connected MCP server.
     * This is essential for LLMs to understand what capabilities are available.
     * 
     * @return List of function definitions that can be called
     */
    List<FunctionDefinition> discover();
    
    /**
     * Execute a specific function on the MCP server with provided parameters.
     * This is how LLMs invoke the server's capabilities.
     * 
     * @param functionName Name of the function to execute
     * @param parameters Parameters to pass to the function
     * @return Result object from the function execution
     */
    Object execute(String functionName, Map<String, Object> parameters);
    
    /**
     * Close the connection to the MCP server gracefully.
     */
    void close();
}