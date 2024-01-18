import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataProducer {
    private Producer<String, String> producer;
    private String traceFileName;

    public DataProducer(Producer producer, String traceFileName) {
        this.producer = producer;
        this.traceFileName = traceFileName;
    }

    /**
      Task 1:
        In Task 1, you need to read the content in the tracefile we give to you, 
        create two streams, and feed the messages in the tracefile to different 
        streams based on the value of "type" field in the JSON string.

        Please note that you're working on an ec2 instance, but the streams should
        be sent to your samza cluster. Make sure you can consume the topics on the
        master node of your samza cluster before you make a submission.
    */
    /**
     * Reads data from the trace file and sends it to the appropriate Kafka topics.
     * The method determines the target topic based on the "type" field in each JSON line.
     * Calculates the partition based on the "blockId" field.
     *
     * @throws IOException If an I/O error occurs while reading the file
     */
    public void sendData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(traceFileName));
        String line;
        JsonParser parser = new JsonParser();

        while ((line = reader.readLine()) != null) {
            JsonObject json = parser.parse(line).getAsJsonObject();
            String type = json.get("type").getAsString();
            int blockId = json.get("blockId").getAsInt();

            String topic;
            switch (type) {
                case "DRIVER_LOCATION":
                    topic = "driver-locations";
                    break;
                case "LEAVING_BLOCK":
                case "ENTERING_BLOCK":
                case "RIDE_REQUEST":
                case "RIDE_COMPLETE":
                    topic = "events";
                    break;
                default:
                    // Handle unexpected type value
                    continue;
            }
            int partition = blockId % 5;  // Calculate the partition number

            // Send the data to the Kafka topic
            producer.send(new ProducerRecord<>(topic, partition, String.valueOf(blockId), line));
        }
        reader.close();
    }

}
