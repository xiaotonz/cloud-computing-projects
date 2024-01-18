import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class DataProducerRunner {

    public static void main(String[] args) throws Exception {
        /*
            Tasks to complete:
            - Write enough tests in the DataProducerTest.java file
            - Instantiate the Kafka Producer by following the API documentation
            - Instantiate the DataProducer using the appropriate trace file and the producer
            - Implement the sendData method as required in DataProducer
            - Call the sendData method to start sending data
        */
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.31.3.54:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Initialize Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        // Replace with the path to your trace file
        String traceFileName = "trace_task2";

        // Initialize DataProducer and send data
        DataProducer dataProducer = new DataProducer(producer, traceFileName);
        dataProducer.sendData();

        // Close the producer
        producer.close();
            
    }
}
