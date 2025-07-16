package com.watchtower.functions;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

/**
 * Represents a function call request from the LLM
 */
@Data
@AllArgsConstructor
public class FunctionCall {
    private String name;
    private Map<String, Object> arguments;
}