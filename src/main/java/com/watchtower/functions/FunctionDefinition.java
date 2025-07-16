package com.watchtower.functions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Describes a function that can be called by the LLM
 */
@Data
@Builder
@AllArgsConstructor
public class FunctionDefinition {
    private String name;
    private String description;
    private List<Parameter> parameters;
    
    @Data
    @AllArgsConstructor
    public static class Parameter {
        private String name;
        private String type;  // "string", "integer", "boolean"
        private String description;
        private boolean required;
        
        public Parameter(String name, String type, String description) {
            this(name, type, description, true);
        }
    }
}