package com.watchtower.model;

import lombok.Value;
import java.time.Instant;
import java.util.Map;

/**
 * Represents a metric data point
 */
@Value
public class Metric {
    Instant timestamp;
    String name;
    double value;
    String unit;
    Map<String, String> labels;
}