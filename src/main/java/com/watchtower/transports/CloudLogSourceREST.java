package com.watchtower.transports;

/**
 * AWS exposes their CloudLogSource via REST
 * 
 * Notice: They need to translate function definitions to OpenAPI format
 */
public class CloudLogSourceREST {
    // REST endpoint: POST /functions/execute
    // {
    //   "function": "fetchLogs",
    //   "arguments": {
    //     "resource": "payment-service",
    //     "filter": "ERROR",
    //     "limit": 1000
    //   }
    // }
    
    // OpenAPI spec needed for function discovery
    // Different format than our FunctionDefinition!
}