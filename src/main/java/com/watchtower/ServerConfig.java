package com.watchtower;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Configuration for connecting to a protocol server
 */
@Data
@RequiredArgsConstructor
public class ServerConfig {
    private final String host;
    private final int port;
    private final Map<String, String> credentials;
}