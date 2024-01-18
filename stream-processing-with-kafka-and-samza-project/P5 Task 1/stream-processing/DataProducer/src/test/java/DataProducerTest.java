import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DataProducerTest {
    private MockProducer<String, String> producer;

    @Before
    public void setUp() {
        producer = new MockProducer<>(
                true, new StringSerializer(), new StringSerializer());
    }

    /**
     * This test checks if the messages go to the correct topic and partition as required.
     * Additional test cases can be added by adding more entries to test_trace and verifying here. 
     * @throws IOException
     */
    @Test
    public void testProducer() throws IOException {
        DataProducer dataProducer = new DataProducer(producer, "test_trace");

        dataProducer.sendData();

        List<ProducerRecord<String, String>> history = producer.history();

        List<ProducerRecord<String, String>> expected = Arrays.asList(
                new ProducerRecord<>("events", 3, null, "{\"blockId\":5648,\"type\":\"ENTERING_BLOCK\"}"),
                new ProducerRecord<>("driver-locations", 4, null, "{\"blockId\":5649,\"type\":\"DRIVER_LOCATION\"}"));

        Assert.assertEquals("Producer records not matched!", expected, history);
    }

    /**
     * Test to ensure messages with type ENTERING_BLOCK are sent to the events topic.
     */
    @Test
    public void testEnteringBlockMessages() throws IOException {
        DataProducer dataProducer = new DataProducer(producer, "test_trace1");

        dataProducer.sendData();

        // Expecting ENTERING_BLOCK events in the 'events' topic
        Assert.assertTrue("Missing ENTERING_BLOCK event in events topic",
                producer.history().stream()
                        .anyMatch(record -> record.topic().equals("events") &&
                                record.value().contains("\"type\":\"ENTERING_BLOCK\"")));
    }

    /**
     * Test to ensure messages with type RIDE_REQUEST are sent to the events topic.
     */
    @Test
    public void testRideRequestMessages() throws IOException {
        DataProducer dataProducer = new DataProducer(producer, "test_trace1");

        dataProducer.sendData();

        // Expecting RIDE_REQUEST events in the 'events' topic
        Assert.assertTrue("Missing RIDE_REQUEST event in events topic",
                producer.history().stream()
                        .anyMatch(record -> record.topic().equals("events") &&
                                record.value().contains("\"type\":\"RIDE_REQUEST\"")));
    }

    /**
     * Test to ensure messages with type LEAVING_BLOCK are sent to the events topic.
     */
    @Test
    public void testLeavingBlockMessages() throws IOException {
        DataProducer dataProducer = new DataProducer(producer, "test_trace1");

        dataProducer.sendData();

        // Expecting LEAVING_BLOCK events in the 'events' topic
        Assert.assertTrue("Missing LEAVING_BLOCK event in events topic",
                producer.history().stream()
                        .anyMatch(record -> record.topic().equals("events") &&
                                record.value().contains("\"type\":\"LEAVING_BLOCK\"")));
    }

    /**
     * Test to ensure messages with type RIDE_COMPLETE are sent to the events topic.
     */
    @Test
    public void testRideCompleteMessages() throws IOException {
        DataProducer dataProducer = new DataProducer(producer, "test_trace1");

        dataProducer.sendData();

        // Expecting RIDE_COMPLETE events in the 'events' topic
        Assert.assertTrue("Missing RIDE_COMPLETE event in events topic",
                producer.history().stream()
                        .anyMatch(record -> record.topic().equals("events") &&
                                record.value().contains("\"type\":\"RIDE_COMPLETE\"")));
    }
}
