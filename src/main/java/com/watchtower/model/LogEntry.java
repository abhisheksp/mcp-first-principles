package com.watchtower.model;

import java.time.Instant;

public record LogEntry(
    Instant timestamp,
    String message,
    String severity,
    String source
) {}