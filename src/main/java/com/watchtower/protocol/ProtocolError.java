package com.watchtower.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard JSON-RPC error format
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolError {
    private int code;
    private String message;
    private Object data;
    
    // Standard error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
}