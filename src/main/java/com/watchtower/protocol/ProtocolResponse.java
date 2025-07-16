package com.watchtower.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON-RPC 2.0 Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolResponse {
    @Builder.Default
    private String jsonrpc = "2.0";
    private Object result;
    private ProtocolError error;
    private String id;
    
    public static ProtocolResponse success(String id, Object result) {
        return ProtocolResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .result(result)
            .build();
    }
    
    public static ProtocolResponse error(String id, int code, String message) {
        return ProtocolResponse.builder()
            .jsonrpc("2.0")
            .id(id)
            .error(new ProtocolError(code, message, null))
            .build();
    }
}