package com.cloudcomputing.samza.nycabs.application;

import com.cloudcomputing.samza.nycabs.DriverMatchTask;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.apache.samza.application.TaskApplication;
import org.apache.samza.application.descriptors.TaskApplicationDescriptor;
import org.apache.samza.serializers.JsonSerde;
import org.apache.samza.system.kafka.descriptors.KafkaInputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaOutputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaSystemDescriptor;
import org.apache.samza.task.StreamTaskFactory;
import com.google.gson.JsonObject;

public class DriverMatchTaskApplication implements TaskApplication {
    // Consider modify this zookeeper address, localhost may not be a good choice.
    // If this task application is executing in slave machine.
    private static final List<String> KAFKA_CONSUMER_ZK_CONNECT = ImmutableList.of("172.31.3.54:2181");

    // Consider modify the bootstrap servers address. This example only cover one
    // address.
    private static final List<String> KAFKA_PRODUCER_BOOTSTRAP_SERVERS = ImmutableList.of("172.31.9.125:9092","172.31.3.201:9092","172.31.3.54:9092");
    private static final Map<String, String> KAFKA_DEFAULT_STREAM_CONFIGS = ImmutableMap.of("replication.factor", "1");
    private static final String KAFKA_SYSTEM_NAME = "kafka";
    private static final String DRIVER_LOCATIONS_TOPIC = "driver-locations";
    private static final String EVENTS_TOPIC = "events";
    private static final String MATCH_STREAM_TOPIC = "match-stream";

    @Override
    public void describe(TaskApplicationDescriptor taskApplicationDescriptor) {
        // Define a system descriptor for Kafka.
        KafkaSystemDescriptor kafkaSystemDescriptor = new KafkaSystemDescriptor("kafka")
                .withConsumerZkConnect(KAFKA_CONSUMER_ZK_CONNECT)
                .withProducerBootstrapServers(KAFKA_PRODUCER_BOOTSTRAP_SERVERS)
                .withDefaultStreamConfigs(KAFKA_DEFAULT_STREAM_CONFIGS);

        // Hint about streams, please refer to DriverMatchConfig.java
        // We need two input streams "events", "driver-locations", one output stream
        // "match-stream".

        // Define your input and output descriptor in here.
        // Reference solution:
        // https://github.com/apache/samza-hello-samza/blob/master/src/main/java/samza/examples/wikipedia/task/application/WikipediaStatsTaskApplication.java

        // Bound you descriptor with your taskApplicationDescriptor in here.
        // Please refer to the same link.

        KafkaInputDescriptor eventsInputDescriptor =
            kafkaSystemDescriptor.getInputDescriptor("events", new JsonSerde<>());

        KafkaInputDescriptor locInputDescriptor =
            kafkaSystemDescriptor.getInputDescriptor("driver-locations", new JsonSerde<>());
            
        KafkaOutputDescriptor matchOutputDescriptor =
                kafkaSystemDescriptor.getOutputDescriptor("match-stream", new JsonSerde<>());

        // Set the default system descriptor to Kafka, so that it is used for all
        // internal resources, e.g., kafka topic for checkpointing, coordinator stream.
        taskApplicationDescriptor.withDefaultSystem(kafkaSystemDescriptor);

        // Set the input
        taskApplicationDescriptor.withInputStream(eventsInputDescriptor);
        taskApplicationDescriptor.withInputStream(locInputDescriptor);
        // Set the output
        taskApplicationDescriptor.withOutputStream(matchOutputDescriptor);
        
        // Bound you descriptor with your taskApplicationDescriptor in here.
        // Please refer to the same link.
        // Set the task factory
        taskApplicationDescriptor.withTaskFactory((StreamTaskFactory)() -> new DriverMatchTask());
    }

    
}
