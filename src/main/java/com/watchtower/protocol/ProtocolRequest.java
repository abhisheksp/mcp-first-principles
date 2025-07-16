package com.watchtower.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * JSON-RPC 2.0 Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolRequest {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String method;
    private Map<String, Object> params;
    private String id;
}