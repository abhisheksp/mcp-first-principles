package com.watchtower.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class ProtocolMessages {
    
    @Data
    @NoArgsConstructor
    public static class Request {
        private String jsonrpc = "2.0";
        private String method;
        private JsonNode params;
        private String id;
        
        public Request(String jsonrpc, String method, JsonNode params, String id) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            this.method = method;
            this.params = params;
            this.id = id;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String jsonrpc = "2.0";
        private JsonNode result;
        private Error error;
        private String id;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error {
        private int code;
        private String message;
        private JsonNode data;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitializeParams {
        private Map<String, String> credentials;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscoverResult {
        private List<FunctionInfo> functions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionInfo {
        private String name;
        private String description;
        private List<ParameterInfo> parameters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterInfo {
        private String name;
        private String type;
        private String description;
        private boolean required;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecuteParams {
        private String function;
        private Map<String, Object> arguments;
    }
}