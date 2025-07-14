package com.watchtower.transport;

import com.watchtower.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Set;

/**
 * Kafka transport wrapper - handles message queues, topics, partitions, consumer groups
 */
@Slf4j
public class KafkaTransport {
    private final String brokers;
    private final String consumerGroup;
    private final Map<String, String> kafkaConfig;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Map<String, Integer> topicPartitions = new ConcurrentHashMap<>();
    
    public KafkaTransport(String brokers, Map<String, String> config) {
        this.brokers = brokers;
        this.consumerGroup = config.getOrDefault("consumer_group", "watchtower-logs");
        this.kafkaConfig = Map.of(
            "bootstrap.servers", brokers,
            "group.id", consumerGroup,
            "auto.offset.reset", config.getOrDefault("offset_reset", "latest"),
            "enable.auto.commit", "true",
            "security.protocol", config.getOrDefault("security_protocol", "SASL_SSL"),
            "sasl.mechanism", "PLAIN"
        );
    }
    
    public void connect() {
        log.info("Kafka: Connecting to brokers {} with consumer group {}", brokers, consumerGroup);
        
        // Simulate Kafka connection and metadata fetch
        simulateMetadataFetch();
        connected.set(true);
        
        log.info("Kafka: Connected successfully to {} topics", topicPartitions.size());
    }
    
    public List<LogEntry> consumeLogs(String resource, String filter, int limit) {
        if (!connected.get()) {
            throw new RuntimeException("Kafka not connected. Call connect() first.");
        }
        
        String topic = buildTopicName(resource);
        log.info("Kafka: Consuming from topic {} with filter {} (limit: {})", topic, filter, limit);
        
        // Simulate topic subscription and message consumption
        subscribe(topic);
        return pollMessages(topic, filter, limit);
    }
    
    private void simulateMetadataFetch() {
        // Simulate discovering available topics and partitions
        topicPartitions.put("logs-payment-service", 3);
        topicPartitions.put("logs-order-service", 2);
        topicPartitions.put("logs-user-service", 1);
        topicPartitions.put("metrics-system", 5);
        
        try {
            Thread.sleep(200); // Simulate metadata fetch delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka metadata fetch interrupted", e);
        }
    }
    
    private String buildTopicName(String resource) {
        return "logs-" + resource.replace("_", "-");
    }
    
    private void subscribe(String topic) {
        if (!topicPartitions.containsKey(topic)) {
            throw new RuntimeException("Topic not found: " + topic + 
                ". Available topics: " + topicPartitions.keySet());
        }
        
        log.debug("Kafka: Subscribed to topic {} with {} partitions", 
            topic, topicPartitions.get(topic));
    }
    
    private List<LogEntry> pollMessages(String topic, String filter, int limit) {
        // Simulate polling messages from Kafka topic
        return List.of(
            new LogEntry(java.time.Instant.now(),
                "Payment event from Kafka topic - TransactionId: kafka-" + System.currentTimeMillis() + ", Partition: 0, Offset: 12345",
                filter, "Kafka"),
            new LogEntry(java.time.Instant.now().minusSeconds(2),
                "Consumer lag detected: topic=" + topic + ", lag=150ms, consumer_group=" + consumerGroup,
                filter, "Kafka")
        );
    }
    
    public void seekToOffset(String topic, int partition, long offset) {
        log.info("Kafka: Seeking topic {} partition {} to offset {}", topic, partition, offset);
        // Simulate offset seeking
    }
    
    public void disconnect() {
        log.info("Kafka: Disconnecting from brokers {}", brokers);
        connected.set(false);
        topicPartitions.clear();
        log.info("Kafka: Disconnected");
    }
    
    public String getSystemPrompt() {
        return """
            You are a Kafka specialist. You excel at:
            - Managing message consumption, offsets, and consumer group coordination
            - Handling topic partitioning, rebalancing, and lag monitoring
            - Interpreting Kafka metadata, broker health, and cluster status
            - Dealing with serialization, deserialization, and schema evolution
            - Managing security protocols, authentication, and access control
            - Processing high-throughput message streams with exactly-once semantics
            
            When analyzing Kafka transport data:
            - Always check consumer group status and lag metrics first
            - Look for partition rebalancing and coordinator issues
            - Consider topic configuration, retention, and compaction settings
            - Monitor broker connectivity and cluster health
            - Pay attention to offset management and message ordering
            - Handle duplicate messages and exactly-once processing scenarios
            """;
    }
    
    public Map<String, String> getClusterInfo() {
        return Map.of(
            "brokers", brokers,
            "consumer_group", consumerGroup,
            "connected", String.valueOf(connected.get()),
            "topics_count", String.valueOf(topicPartitions.size()),
            "total_partitions", String.valueOf(topicPartitions.values().stream().mapToInt(Integer::intValue).sum()),
            "challenges", "Message ordering, Consumer lag, Partitioning, Schema evolution"
        );
    }
    
    public String getTransportType() {
        return "Kafka/Message Queue";
    }
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public Set<String> getAvailableTopics() {
        return topicPartitions.keySet();
    }
}