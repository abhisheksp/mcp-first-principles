package com.watchtower.functions;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Result of executing a function
 */
@Data
@AllArgsConstructor
public class FunctionResult {
    private String functionName;
    private Object result;
    private boolean success;
    private String error;
    
    public static FunctionResult success(String functionName, Object result) {
        return new FunctionResult(functionName, result, true, null);
    }
    
    public static FunctionResult error(String functionName, String error) {
        return new FunctionResult(functionName, null, false, error);
    }
}