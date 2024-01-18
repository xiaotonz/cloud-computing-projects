# P5. Stream Processing with Kafka and Samza

# Introduction

Over the past few years, many industries have managed to benefit financially from processing real-time streams of data. Hence, data has moved from being generated and processed at infrequent intervals to being processed in real-time. This places extreme latency and throughput requirements which simply cannot be managed efficiently by batch-based frameworks such as MapReduce or Apache Spark unless specific techniques or libraries are used. In this project we will explore one way of managing and processing a large amount of real-time data with low latency, using a **stream processing framework**.

Traditional data processing progresses through several stages, in what is typically called the **data processing pipeline**. You have seen this pattern already in previous projects. First, data from the business (from sources such as an OLTP database or log files) is collected and stored for offline processing. This data is then cleaned (through an ETL job) and fed to multiple systems (which may have different data formats) to perform batch processing and the output is then visualized or fed to other systems forming a data pipeline. This method of data processing works well for historical data and for data that does not lose much of its "value" over the time it takes to process it. For example, a traditional retailer can use weekly or monthly sales trends to plan and improve their future warehouse stocking orders through batch processing and analytics. However, another retailer might need to make quick decisions about the effectiveness of certain discounts and coupons at driving sales during Cyber Monday which will require real-time or near real-time processing.

Recently there has been an increased interest in real-time data. For example, as you are viewing a social network feed, a decision has to be made about which advertisement to show you next, based on which parts of the feed you are slowing down to read, as well as your profile and other available data. Real-time data sources include sensor data (primarily due to the explosion of the Internet of Things (IoT) devices), social network interactions, business-critical data generated in real-time, and others. These events need to be processed with extremely low latency in the order of milliseconds - failing to do so would cause the company to lose its competitive edge in the market. LinkedIn uses real-time feeds of ad click data and ad impressions data to feed its ad infrastructure. Using MapReduce or other batch-processing systems is possible but a bad fit for such use cases since a decision needs to be made within the order of milliseconds. This is where stream processing systems come in, having specifically been designed to process billions of events every day with low latency. Apache Kafka and Apache Samza are two such systems that enable processing a stream of data in real-time (with low latency).

Kafka is a distributed publish-subscribe messaging system and Samza is a distributed stream processing system. For an introduction and overview of Kafka and Samza, please refer to the primer.



Danger



### Intense Project Warning

You should start this project early to avoid any potential roadblocks before the deadline. This project consists of complex logic and extensive debugging especially Task 2 and Task 3.





Task 1

# Task 1: Create Streams using Kafka Producer

After dabbling in many companies this semester you decide you want to work for another company before deciding where you will stay. NYCabs is an upcoming private cab/rideshare app. You are hired to implement the core part of the service, that of matching client requests to available drivers and displaying advertisements. Ride-hailing apps like Uber have the driver send position updates roughly every 5 seconds which forms a large stream of data. After matching the client, the company will choose an advertisement to display for that client. This decision involves analyzing the client's profile information and the business's information. The company could also get clients’ interest from their phones based on their browser data. Utilizing all this information could help the company to develop a decision-making system for advertising.

One way to handle the position and interest updates is to keep updating a traditional data store (MySQL/HBase) with the positions, and when a rider request comes in, look up the location of the rider and match the closest driver to the client. This approach is not very scalable even with sharding and/or replication and is wasteful since once the driver has moved to a new position 5 seconds later or new interest for that user arrives, the old data is useless. Being well-informed about the latest cloud technologies, you decide to use the stream processing model of computation since it fits this use case very well.

### Introduction

In this task, you will write the code for the Kafka Producer to create streams using the trace file provided to you. This task aims to simulate the scenario where drivers update their locations regularly as they move in the city and the clients' requests for rides arrive at some time. Earlier, you have played with Kafka commands locally. In this task, you will develop a Java program that sends messages to Kafka topics on a remote(AWS) Samza cluster.

You need to create two topics named `driver-locations` and `events` using the shell command, and then read the tracefile and send the trace records to the topics using the Java API. Each line in the tracefile is a JSON string containing various fields. Both streams may have different fields, but you can assume each line must have `type` and `blockId` fields. In Task 1, you will create two topics based on the value of field `type`, and partition the streams based on the value of field `blockId`. The number of partitions should be `5`. **You must use `blockId modulo 5` to calculate the partition number for a message**.

The following table lists the configuration for each topic and also explains the field of each stream.



Information

Based on the value of `type` field, each line belongs to either `driver-locations` topic or `events` topic.





| Stream name          | Value of the field `type` in JSON string                   |
| :------------------- | :--------------------------------------------------------- |
| `driver-locations`   | DRIVER_LOCATION                                            |
| `events`             | LEAVING_BLOCK, ENTERING_BLOCK, RIDE_REQUEST, RIDE_COMPLETE |
|                      |                                                            |
| Number of partitions | 5                                                          |
| Replication factor   | 1                                                          |

**Table 1:** Type and corresponding topic name.



**Please note that you will work on a separate VM instance from your Samza cluster(EMR). You need to write code to feed messages to the Kafka topics that reside on your remote Samza cluster. On the EMR cluster, you can use Kafka commands to create topics.**

![Task 1](https://clouddeveloper.blob.core.windows.net/assets/kafka-samza-taxicabs/images/task1.png)

**Figure 1:** Task 1.

**In Task 1, you only need to feed the `entire line` to the appropriate topic.**

For more details on each stream, refer to the Information box below.



Information



In Task 1, you only need to utilize the `type` field and output the **entire line** to the corresponding Kafka topics. The following details will help you understand the structure of the streams. You may need to go through them carefully when you work on Task 2.

#### **driver-locations stream**

This stream is a stream of free driver locations as they move through the city.

You need to publish this stream in Kafka under the topic `driver-locations`.

Each JSON string contains the following fields:



| Field                   | Type      | Value                                                        |
| :---------------------- | :-------- | :----------------------------------------------------------- |
| `blockId`               | `integer` | the block where the driver is currently moving. This is similar to a city block/neighborhood. A block can have multiple drivers. The stream is partitioned on this field. |
| `driverId`              | `integer` | unique identifier of the driver.                             |
| `type`                  | `string`  | `DRIVER_LOCATION` for this particular stream.                |
| `latitude`, `longitude` | `float`   | within a block a driver will be at a particular latitude and longitude. This latitude and longitude are what you will consider as a part of the match score to find a driver for a given client request. |

**Table 2:** driver-locations stream fields explanation.



**example:**

```
  {"driverId":131,"blockId":3214,"latitude":40.7519871,"longitude":-74.0047584,"type":"DRIVER_LOCATION"}
```

#### **events stream**

This stream is a stream of events that are separate from the driver location updates.

This includes events from both clients and drivers.

Publish this stream in Kafka under the topic `events`.

Each JSON string contains the following fields:



| Field                   | Type      | Value                                                        |
| :---------------------- | :-------- | :----------------------------------------------------------- |
| `blockId`               | `integer` | the block where the user (driver or rider) is currently present. The stream is partitioned on this field. |
| `clientId`/`driverId`   | `integer` | unique identifier of the driver or client. This will be clientId if the type of event is `RIDE_REQUEST`. It will be driverId in all other cases. |
| `type`                  | `string`  | `LEAVING_BLOCK` - a driver is moving to a different block or is going offline. This event will come with the blockId, latitude and longitude of the old block. Use this to update your local state for the old block. For example, if a driver is moving from block 1 to block 2, this event will arrive with the blockId 1.`ENTERING_BLOCK` - a driver is logging in or is entering a different block. This event will come with the blockId, latitude and longitude of the new block. Use this to update your local state for the new block. For example, if a driver is moving from block 1 to block 2, this event will arrive with the blockId 2.`RIDE_REQUEST` - a client has requested a ride in a particular block. Find the driver with the highest match score and output that driverId to the output stream.`RIDE_COMPLETE` - a ride has completed. It comes with the current location of the driver, meaning this driver is available again in this block. |
| `latitude`, `longitude` | `float`   | within a block, a driver will be at a particular latitude and longitude, so you can find these two attributes in the events of `ENTERING_BLOCK`, `LEAVING_BLOCK` and `RIDE_COMPLETE`. A client also has his or her latitude and longitude when s/he requests a ride. These attributes also appear in the event of `RIDE_REQUEST`. |
| `gender`                | `string`  | this attribute specifies the gender of the driver. In this project, the gender of a driver is either "M" for male or "F" for female. You will see this attribute in the events of `ENTERING_BLOCK` and `RIDE_COMPLETE`. |
| `gender_preference`     | `string`  | the counterpart of the attribute gender. In the event of `RIDE_REQUEST`, a client will come with his or her driver gender_preference ("M" for male, "F" for female and "N" for no preference). |
| `rating`                | `float`   | each driver has a float number from 0.0 to 5.0 (inclusive) as his or her rating. The rating also contributes to the match score since a client always wants a driver with a high reputation. You can find this attribute in `ENTERING_BLOCK` and `RIDE_COMPLETE`. |
| `salary`                | `integer` | the amount of money this driver has made today. Salary is an integer from 0 to 100 (inclusive) and you can see it in the events of `ENTERING_BLOCK` and `RIDE_COMPLETE`. |
| `status`                | `string`  | This field is valid ONLY if the type is `LEAVING_BLOCK` or `ENTERING_BLOCK`. It persists the state of the driver (free or busy) across blocks. The valid values for this field are "AVAILABLE" and "UNAVAILABLE". |
| `user_rating`           | `float`   | This field is valid ONLY if type is `RIDE_COMPLETE`. It is the client's rating for this ride. The driver's rating should be updated to the average of `rating` and `user_rating`. In other words, the formula to update rating is (`old_rating` + `user_rating`)/2. |

**Table 3:** events stream fields explanation.



**Some examples:**

```
{"blockId":5647,"driverId":7806,"latitude":40.7901188,"longitude":-73.9747985,"type":"ENTERING_BLOCK","status":"AVAILABLE","rating":2.14,"salary":11,"gender":"F"}

{"blockId":1930,"clientId":6343,"latitude":40.731471,"longitude":-73.9901805,"type":"RIDE_REQUEST","gender_preference":"N"}

{"blockId":1113,"driverId":4843,"latitude":40.7182511,"longitude":-74.0053824,"type":"ENTERING_BLOCK","status":"UNAVAILABLE","rating":3.7,"salary":26,"gender":"M"}

{"blockId":6,"driverId":3602,"latitude":40.7014372,"longitude":-74.0119515,"type":"LEAVING_BLOCK","status":"UNAVAILABLE"}

{"blockId":1544,"driverId":8429,"latitude":40.7258816,"longitude":-73.9775455,"type":"RIDE_COMPLETE","gender":"M","rating":4.79,"user_rating":4.0,"salary":25}
```





### Test-Driven Development

We expect you to follow a Test-Driven Development (TDD) approach for building your Kafka broker. This will help you test your code locally by checking that the messages have correctly reached the correct topic and partition. The best thing about this is you can test your logic without spinning up a costly EMR cluster, which will allow you to save your budget. You can run the tests using the following command.

```
mvn test
```

It is highly encouraged that you write good tests of your own. We will **manually grade** for at least one test you have written. The tests can be written in `stream-processing/DataProducer/src/test/java/DataProducerTest.java`.

### Setting up the Workspace and EMR cluster

Like what you have done in other projects, we encourage you to use Terraform to provision the resources for this project.

1. In this project, you will need a `t3.micro` EC2 workspace instance, and an EMR cluster with 1 master node and 2 slave nodes all using `m4.large` instances. You can launch the resources by following the steps below,

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/kafka-samza-taxicabs/templates/aws-emr.tgz
   tar -xvzf aws-emr.tgz
   
   # 1. Run `aws configure` to setup your AWS environment
   # 2. Create a terraform.tfvars file and set the key_name variable (without the '.pem' extension and without the path to the secret PEM key)
   # e.g. key_name="CC_Key_Pair"
   
   terraform init
   terraform apply
   ```

   **Note:** If the terraform command fails with **EMR_EC2_DefaultRole** error, you can try running

   ```
       aws emr create-default-roles
   ```

   If this does not work, you may check out other troubleshooting steps mentioned in [this AWS Documentation](https://aws.amazon.com/premiumsupport/knowledge-center/emr-default-role-invalid/) to create the required role.

2. The terraform script will output the private IP of the master node and the private IPs of the brokers. Make a note of these IPs as you will be using them in the tasks and also during task submission.

3. Always destroy the resources launched by terraform through terraform, instead of terminating the instances manually through the web console or CLI. **Note:** If you only terminate the spot instances without removing the spot fleet first, the fleet will launch new spot instances automatically.

### Steps to complete the task

1. Launch an EMR cluster and a workspace EC2 instance using terraform based on the above instructions.

2. Setup Kafka and Samza on the EMR cluster by following the `Setting up the Kafka & Samza` section in the **Introduction to Kafka and Samza** primer.

3. Create two topics `driver-locations` and `events` in the EMR cluster as per the requirements in Table 1, by following the instructions for creating a new topic in the **Introduction to Kafka and Samza** primer.

4. **You can skip steps 4 - 6 if you used terraform to launch your instance. But, make sure that you have the security groups and tags properly assigned.** Launch an *on-demand* `t3.micro` EC2 instance in the `us-east-1` region from the web console. Use the AMI named as "Cloud Computing Project Image" with the AMI ID `ami-04537cfe22bace769` which can be found under the "Community AMIs" tab. If you are going to use spot pricing at this step. Note that the tags of a spot request will NOT propagate to the instance hence you need to manually tag the instance once it is created. We would suggest that you use an on-demand instance for this step.

5. Go to the "Security Groups" page in the EC2 dashboard. Click the "Create Security Group" button to create a security group, enter a security group name and description. Then add inbound rules to allow ingress traffic to ports `22` and `80`. Note that you are only allowed to open ports 22 and 80 to the public. Click the "create" button to create the security group.

6. When launching the instance, under Step 6 of the "Configure Security Group" page, choose "Select an existing security group", then select `emr_master`, `emr_slave` and the security group you created in the last step to attach 3 security groups in total to the instance. **Remember to tag your EC2 instance** and click the "review and launch" button to launch your instance.

7. Prepare your student VM instance:

   - Open the URL `http://[your-workspace-public-DNS]` in your web browser.
   - Enter your submission username and submission password. Choose the `Stream Processing with Kafka and Samza` project.
   - You will see some logs indicating that files are being transferred and your environment is being prepared. If an error is reported, please create a private Piazza post to inform the staff.

8. If Step 7 was successful, log into the instance with `clouduser` using PEM/PPK file.

9. The input tracefile will be located in the `stream-processing/DataProducer` directory. You can use the `head` command to inspect several lines in order to better understand the content.

10. To add the logic, you will need to modify the code under

    - `stream-processing/DataProducer/src/main/java/DataProducer.java` and
    - `stream-processing/DataProducer/src/main/java/DataProducerRunner.java`. **Please refer to Kafka producer [API](https://kafka.apache.org/0101/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html) to understand how to implement this part.**

11. Test your program by running `mvn test`. **Don't forget to add your own test cases by adding entries to the test_trace file.**

12. Before running the Producer code, you will need to make sure that you successfully created all the required topics with the required number of partitions. Running the below command will execute the Producer code and remotely feed messages to the partitions you created in the Samza cluster.

    ```
    mvn compile exec:java -Dexec.cleanupDaemonThreads=false
    ```

    You can also to manually verify if the data is being fed into the streams using `kafka-console-consumer.sh`. If your Producer code is correct, you will be able to see the message stream that the Kafka Producer is producing when you run the consumer.

13. It may take several minutes to feed all the messages to finish being sent when you run the Kafka Producer. After your program completes, complete the following steps to submit Task 1 for autograding.

#### How to submit

Please follow the steps to submit your project:

1. Export the required credentials

   ```
   export HISTIGNORE=export* # so that the following export commands will not be tracked into bash history
   export SUBMISSION_USERNAME=your_submission_username
   export SUBMISSION_PASSWORD=your_submission_pwd
   ```

2. After you have successfully fed all the messages into the two streams by running the Producer code, execute `./submitter_task1` to make a submission.

3. When prompted, enter the private IP of the master and brokers. This step may take a while since we will be reading a large number of events from your Samza cluster.

**Note:** **The submitter will read from the beginning of the streams**. So make sure you don't have legacy messages in your streams when you re-run your program. You may refer to the Kafka command line tools in the **Introduction to Kafka and Samza** primer.

#### Hints

1. If the submitter seems to hang for a long time, it is because there is nothing on the output stream. Check the name of the two streams and verify them using `kafka-console-consumer.sh`.
2. You should run `mvn test` on the student VM, and not on your own machine.
3. Every time you create the student VM workspace, a new tracefile will be generated. Please always use the latest tracefile.

Task 2

# Task 2: Consume and analyze streams using Samza

### Introduction

In this task, you will write Samza code to consume the two streams generated by your Kafka stream generator in Task 1 and output the client/driver pair to the `match-stream` stream. The driver must be the one with the highest match score to the client (see the details in the algorithm description below) and **within the same block as the client**.



Information





| Stream name    | Description                                                  |
| :------------- | :----------------------------------------------------------- |
| `match-stream` | This stream will be output by your Samza job. It must be in JSON format which later can be deserialized to `Map<String, Object>`. **Read the Samza java docs if you are uncertain**. It MUST have the following fields:**clientId:** The ***Integer\*** id of the client for whom a ride has been generated.**driverId:** The ***Integer\*** id of the free driver with the highest match score with this client, i.e. the driver who is assigned to this client.**Examples:**{"clientId":902,"driverId":434} |

**Table 5:** Output streams

![Task 1](https://clouddeveloper.blob.core.windows.net/assets/kafka-samza-taxicabs/images/task2.png)

**Figure 2:** Task 2.

![Cab matching](https://clouddeveloper.blob.core.windows.net/assets/kafka-samza-taxicabs/images/p5task2.png)**Figure 3:** Cab matching service. There are 2 candidate drivers in block 1 but driver 1234 has a higher match score. Some fields in the JSON have been omitted.

### Formula for the match score

Besides using the distance to find the closest driver for a client, we also take the gender preference, driver's rating, and salary into consideration. Table 4 shows the how much factor should be weighted when calculating the match score.







| Factors | distance | gender | rating | salary |
| :------ | :------- | :----- | :----- | :----- |
| Weights | 40%      | 10%    | 30%    | 20%    |

**Table 4:** Weights of factors



**distance_score:** Use the **Euclidean distance** between the driver and the client. For each client, we only look for a driver in the same block and we want to convert the distance to a real number score between 0 to 1. The following formula can be a possible way to do so. The `client_driver_distance` is the Euclidean distance between the driver's and the client's positions.

```
distance_score = 1 * e ^ (-1 * client_driver_distance)
```

**gender_score:** Similar to the distance score, we can use a number to measure whether the driver's gender matches the client's preference. If the client's preference equals to the driver's gender, the gender score for this client-driver pair is 1.0; otherwise, the gender score is 0.0. For the client whose gender preference is "N", we assume he or she matches with both genders "M" and "F".

**rating_score:** A driver's rating is a real number between 0.0 and 5.0, so we can standardize it by:

```
rating_score = rating / 5.0
```

**salary_score:** The idea here is that NYCabs wants the money earned to be evenly distributed among drivers so that they don't lose drivers who earn little money every day. In that case, when there are two available drivers, with the other conditions being the same, the driver with the lower salary will have higher priority. In our simple model, we normalize the salary by the maximum possible salary of 100 USD.

```
salary_score = 1 - (salary / 100.0)
```

**match_score:** To calculate the match_score, combine distance_score, gender_score, rating_score and salary_score and their respective weights to calculate a match score. You need to match a driver with the highest match score to every new incoming client

```
match_score = distance_score * 0.4 + gender_score * 0.1 + rating_score * 0.3 + salary_score * 0.2
```

**Note**: If the driver is not present in the KV store and a driver location event is received, you can either initialize the missing fields like salary to 0, rating to 0.0, and gender to 'N' or skip adding these fields.

### PreRequisites

1. Make sure that the EMR master node has the PEM file. If not, you can `scp` it to the master node using the following command.

   ```
   scp -i "<KEY_NAME>.pem" <KEY_NAME>.pem hadoop@<MASTER_PUBLIC_IP>:/home/hadoop
   ```

2. Download the starter code file for Task 2 to the master node.

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/kafka-samza-taxicabs/templates/driver-match.tgz
   ```

3. Untar the starter code tar file.

   ```
   tar xvfz driver-match.tgz
   ```

4. Run the `deploy_task2` script to generate the input tracefile for task 2.

   ```
   cd driver-match && chmod +x ./deploy_task2 && ./deploy_task2
   ```

Once the download is complete, the data for the Kafka producer is available in `/home/hadoop/driver-match/trace_task2`. You will need to re-run the Producer code with the new tracefile. Copy this new tracefile to your student VM instance. You can run the following command in your master node to do this:

```
scp -i ~/<KEY_NAME>.pem /home/hadoop/driver-match/trace_task2 clouduser@<VM_PUBLIC_IP>:~/stream-processing/DataProducer/
```

In `DriverMatchTask.java`, when you call `getMessage()` in your `process` function, you will get a JSON string of the streams with the fields that are described in the Task 1 table.

As you read through the starter code, you will notice that we have already created a KeyValueStore called `driver-loc` in `driver-match.properties` file for you. It uses string as the key and json as the value. You can use the same or modify it and create your own KeyValueStore and change `stores.[name].key.serde` and `stores.[name].msg.serde` if you have different object types for key and value. You can read more about the KeyValueStore in the Samza [documentation](https://samza.apache.org/learn/documentation/versioned/container/serialization.html).

### Steps to complete the task

In the Producer code, update the tracefile in `stream-processing/DataProducer/src/main/java/DataProducerRunner.java` to use the Task 2 tracefile `trace_task2`

The following steps need to be completed on the Samza cluster

1. Navigate to the `driver-match` project folder. The folder will contain the `pom.xml` file.
2. Fill in the necessary internal IPs identified by the placeholder in `driver-match/src/main/config/driver-match.properties`. Keys that should be filled
   - `yarn.package.path`
   - `systems.kafka.consumer.zookeeper.connect`
   - `systems.kafka.producer.bootstrap.servers`
3. Add the necessary configuration and logic to calculate the driver match score for each new client in the following files
   - `driver-match/src/main/java/com/cloudcomputing/samza/nycabs/DriverMatchTask.java`
   - `driver-match/src/main/java/com/cloudcomputing/samza/nycabs/application/DriverMatchTaskApplication.java`
4. Make sure that your code passes the test cases given in the starter code by running `mvn test`. You can modify the test cases as per your design if needed. Note that, avoiding the given test cases will incur a penalty. However, you are encouraged to add more test cases on your own.
5. Deploy a Samza job to the YARN cluster. To do this, you can execute `runner.sh` script provided by us. You can deploy and run the Samza job by executing the `bash runner.sh` command.
6. If the Samza job was deployed successfully, the job will start and wait for messages on both streams. Monitor the YARN UI at `http://[master-public-dns]:8088` and check the logs for any failures. **By default, the port 8088 is not open to the public. Check the "Debugging in Samza" section in the Introduction to Kafka and Samza primer to learn how to access the UI safely**.
7. You can now run the Task 2 submitter to start up the streams. You can also manually verify the output stream using `kafka-console-consumer.sh` before running the submitter.



Danger



If the output stream does not contain all the records, it could be because of a failed Samza job. Please follow the best practice for debugging using the YARN UI as introduced in **Introduction to Kafka and Samza** primer.





#### How to submit

Please follow the steps to submit your project:

1. Export the required credentials on the Samza cluster master node

   ```
   export HISTIGNORE=export* # so that the following export commands will not be tracked into bash history
   export SUBMISSION_USERNAME=your_submission_username
   export SUBMISSION_PASSWORD=your_submission_pwd
   chmod +x submitter_task2
   ```

2. On the Samza cluster, run `submitter_task2` to make the submission. Running the submitter will generate the stream and validate the output stream. This may take a while since we will be sending a large number of events to your Samza job. The best advice to avoid long code-compile-test times is to constantly monitor the YARN application logs for any errors or failures.

3. On the Samza cluster, start the Samza job from another shell when prompted by the submitter. You can run your Samza job manually or use a script that we provided in `/home/hadoop/driver-match/driver-match/runner.sh` to compile, create the tar file, upload it to HDFS and run the job. This step is required because running the Task 2 submitter kills all the existing running jobs.

4. In the Producer code, replace the trace file name with the Task 2 tracefile. Start your Kafka producer from the student-vm when prompted by the submitter. This will begin producing messages to the `driver-locations` and `events` stream.

5. **Note:** Please note that the submitter will kill all the existing YARN applications, delete all Kafka topics you created, and create topics `events` and `driver-locations` with 5 partitions for you. If you use different names for the topics, the submitter will throw an exception because the topics it tries to delete do not exist.

#### Hints

1. If the submitter seems to hang for a long time, it is because there is nothing on the output stream (because of an error in the Samza job). Look at the YARN logs to find out what went wrong.

2. Think about what should be the key for `driver-loc` KeyValueStore. The KeyValueStore support `range` query. You could learn more about the API for this class from [here](https://samza.apache.org/learn/documentation/versioned/api/javadocs/org/apache/samza/storage/kv/KeyValueStore).

3. One implementation idea is to use `driver-loc` to save **current** driver location and information under its blockId.

4. Remember to update driver rating when processing `RIDE_COMPLETE` event.

5. For Task 2, you can compute the distance by directly calculating the euclidean distance based on longitude and latitude.

6. You could use `./start_kafka` if you want to restart Kafka.

7. You may assume that in Task 2, there is no tie in terms of matching score.

8. If there is a new rider request and no available driver in the current block, you should skip this rider request.

9. Use `Map<String, Object>` for json type in `KeyValueStore` to avoid unserializable errors.

10. You can use the following command to list the brokers and their ids. If you find the number of brokers is less than 3, you could run `./start_kafka` to restart Kafka.

    ```
      zookeeper-shell.sh localhost:2181 <<< "ls /brokers/ids"
    ```

11. Please refer to the **Introduction to Kafka and Samza** primer for debugging tips.

12. If you see java.lang.NoClassDefFoundError exception in the logs, it is because of your program exiting due to some error in your consumer code. Check the logs of all the containers to find the exact error.

13. Re-running the `deploy_task2` script will download a different trace file for you and you must make sure that you use the **newest** trace for Task 2 by copying it again to the Kafka producer instance.

Task 3

# Task 3: Consume and analyze streams and static data using Samza

### Introduction

IoT devices are pervasive, they can be found everywhere and you can use them to infer users’ interests from their behavior. We can use a wearable device to monitor and infer a user's current health status. Consider that we have a future high-tech device that can detect a user’s blood sugar, mood, level of stress and how active a user is. Armed with this timely information, NYCabs would like to advertise directly to specific individuals to make sure their target audience will see a suitable and timely advertisement. In Task 2, you matched a stream of riders with a stream of cars. After the matching process, you are tasked to use the users' profiles, their recent browsing interests, and current health status to display suitable and timely advertisements.

The user’s profile and preferences, NYUStore's information are included as static JSON data stored as files. These static files are stored as part of the Java project resource folder. We will provide you with code to load the static data into the key-value store on the Samza cluster. In this task, you will read the static data from the key-value store and process the streaming data to decide which advertisement to place for a specific user. To do this, you will process the events stream to match a user with one potential business so that you can decide which advertisement to place in the car. You will use the match score to match a user with a business using the distance between the user and the business, the user’s age, the user’s purchasing power, the user’s travel frequency, and the user’s interest. The output will be the `ad-stream`.



Information

| Stream name | Description                                                  |
| :---------- | :----------------------------------------------------------- |
| `ad-stream` | This stream will be the output of your Samza job. It must be in JSON format which later can be deserialized to `Map<String, Object>`. It MUST have the following fields:**userId:** The ***Integer\*** id of the user for whom a ride has been generated.**storeId:** The ***String\*** id of the business with the highest match score with this rider, i.e. the advertisement which is assigned to this user.**name:** The ***String\*** name of the business with the highest match score with this rider.**Examples:**{"userId":1,"storeId":"KgpOYAG-r_eDsQXFXt0nnQ","name":"Balthazar Restaurant"} |

**Table 6:** Output streams in Task 3

### Task Details

**This task has an overload of information that you need to carefully understand to implement the logic. We recommened that you take notes that can help you later in the coding process.**

In this task, you will write Samza code to consume the `events` stream generated using the load generator developed in Task 1, and output the client/yelp business pair to the `ad-stream` stream. The advertisement of the Yelp business is the one with the highest match score to the client (see the details of the matching algorithm below).

![Task 3](https://clouddeveloper.blob.core.windows.net/assets/kafka-samza-taxicabs/images/task3.png)

**Figure 4:** Task 3.

You will use have two new JSON files: `UserInfoData.json` and `NYCStore.json` in this task.

The file `UserInfo.json` has user profiles, including the following information. You can find the full list for `interest` [here](https://clouddeveloper.blob.core.windows.net/primers/kafka-samza/text/interest.txt).







| Field          | Type      | Values                               |
| :------------- | :-------- | :----------------------------------- |
| `userId`       | `integer` | all possible non-negative integer    |
| `gender`       | `string`  | M / F                                |
| `age`          | `integer` | 20 / 40 / 60                         |
| `interest`     | `string`  | 'donuts' / 'burgers' / 'steak' / ... |
| `travel_count` | `integer` | 0 ~ 100                              |
| `device`       | `string`  | iPhone 5/iPhone 7/iPhone XS          |
| `type`         | `string`  | `user`                               |

**Table 7:** User profile Fields explanation



The file `NYCStore.json` contains each business's Yelp profile, including the following information.

**NOTE:** Each business will only have one value in the categories field.



| Field          | Type      | Values                               |
| :------------- | :-------- | :----------------------------------- |
| `storeId`      | `string`  | random unique string                 |
| `name`         | `string`  | business's name                      |
| `review_count` | `integer` | review counts for that business      |
| `categories`   | `string`  | 'donuts' / 'burgers' / 'steak' / ... |
| `rating`       | `float`   | 0 ~ 5.0                              |
| `price`        | `string`  | `$` / `$$` / `$$$` / `$$$$`          |
| `latitude`     | `float`   | latitude of business's location      |
| `longitude`    | `float`   | longitude of business's location     |
| `blockId`      | `integer` | block id for this business           |
| `type`         | `string`  | `yelp`                               |

**Table 8:** Business profile Fields explanation



In the starter code provided, you will find the code to read the static files(`UserInfoData.json` and `NYCStore.json`) and load the content in the key-value store. Also, you will see how tags are assigned to a store. Feel free to change the code that reads the static files.

In the event stream, two new events are added, `RIDER_STATUS` and `RIDER_INTEREST`. The `RIDER_STATUS` event has the fields outlined in Table 9. This event provides the health status of the user.



| Field         | Type      | Values                                                       |
| :------------ | :-------- | :----------------------------------------------------------- |
| `userId`      | `integer` | all possible non-negative integer                            |
| `mood`        | `integer` | integer in range 1 - 8, normal level should be 5, the higher the happier |
| `blood_sugar` | `integer` | integer in range 0 - 5, the healthy level should be 3        |
| `stress`      | `integer` | integer in range 1 - 8, the normal level should be 5, the higher means more stressful |
| `active`      | `integer` | integer in range 1 - 3, the higher, the more excited, the less, the more tired. |
| `type`        | `string`  | `RIDER_STATUS`                                               |

**Table 9:** Input rider status streams fields



You will update a user's `mood`, `blood_sugar`, `stress`, `active` information in the user's profile **every time** you encounter a `RIDER_STATUS` event.

Based on the information in the above four fields, you should be able to give one or more tags to each person which could be used in the rider-store matching process. You can find the tag matching metrics in Table 10. The `Metrics` column should be used for determining the tags of the user. Note that each user can get multiple tags. The `Store category` column should be used for determining the tag of the store. For example, a store with the category `steak` will get the tag `energyProviders`. Also, note that each store will get only one tag.



| Tag               | Metrics                                                   | Store category                                               | Brief reason                                                 |
| :---------------- | :-------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `lowCalories`     | blood_sugar > 4 && mood > 6 && active == 3                | `seafood`/`vegetarian`/`vegan`/ `sushi`                      | May want to eat healthy.                                     |
| `energyProviders` | blood_sugar < 2 \|\| mood < 4                             | `bakeries`/`ramen`/`donuts`/`burgers`/`bagels`/ `pizza`/`sandwiches`/`icecream`/`desserts`/`bbq`/ `dimsum`/`steak` | Big meal or high calories.                                   |
| `willingTour`     | active == 3                                               | `parks`/`museums`/`newamerican`/`landmarks`                  | Have energy to walk around or take a trip.                   |
| `stressRelease`   | stress > 5 \|\| active == 1 \|\| mood < 4                 | `coffee`/`bars`/ `wine_bars`/`cocktailbars`/`lounges`        | Caffeine and alcohol can refresh you, or may want to do “stress eating”. |
| `happyChoice`     | mood > 6                                                  | `italian`/`thai`/`cuban`/`japanese`/`mideastern`/ `cajun`/`tapas`/`breakfast_brunch`/`korean`/ `mediterranean`/`vietnamese`/`indpak`/`southern`/ `latin`/`greek`/`mexican`/`asianfusion`/`spanish`/ `chinese` | May want to try new foods from the whole world.              |
| `others`          | Riders who have not been matched to any of the above tags | Stores that not match to any of the tags mentioned above     | Other options for the riders                                 |

**Table 10:** Tag matching metrics



Another event which is newly added is `RIDER_INTEREST`. This event has the fields outlined in Table 11. It provides the user’s interests while browsing websites. You will only update a user's interest in the user's profile when a user's browsing duration is longer than 5 minutes (do not include 5 minutes).



| Field      | Type      | Values                                                       |
| :--------- | :-------- | :----------------------------------------------------------- |
| `userId`   | `integer` | all possible non-negative integers                           |
| `interest` | `string`  | 'donuts' / 'burgers' / 'steak' / ...                         |
| `duration` | `integer` | the time user browses websites that display his interest in ms |
| `type`     | `string`  | `RIDER_INTEREST`                                             |

**Table 11:** Input interest streams fields



### Ad-Matching logic

After processing a `RIDE_REQUEST` event in Task 2, you will need to decide which advertisement to show that specific rider.

1. You will only place advertisements for businesses with a tag that matches the user's tags. If the user can be matched to more than 1 tag, you should iterate through all those tags. Besides, a user with the tag `others` should be matched to the store with the tag `others`.
2. The initial match score for each business will be `review_count` * `rating`.
3. If the category of the store is the same as the user's interest, add **10** to the initial match score.
4. You will assume that a user with an `iPhone XS` can afford businesses with `$$$` and `$$$$`; a user with an `iPhone 7` can afford businesses with `$$`; and user with an `iPhone 5` will afford business with `$`. If your device does not match the price rating of the business, you will reduce your match score. `score = score * ( 1 - abs(price_value - device_value) * 0.1 )`. The value mapping for device_value and price_value can be found in Table 12. For example, if the match score is `1000`, the business is `$$` and this user's device is `iPhone XS`. The match score will become `score = score * (1 - abs(2 - 3) * 0.1) = 900`
5. You will also need to consider the distance between the business and the user. You should assume that young users and users who travel a lot are not concerned about the distance to the business. If the number of a user's trips is higher than 50 times or the user is exactly 20 years old, you do not have to change the match score for businesses within 10 miles(<= 10 miles). For businesses that are far away from the user (> 10 miles), you will reduce the match score to 10%. In other words, `score = score * 0.1`. For all other users ( > 20 years old and travel count <= 50), the threshold for the distance is 5 miles. If the business is more than 5 miles away from the user, you will reduce the match score to 10%. The code to calculate the distance based on latitude and longitude can be found [here](https://www.geodatasource.com/developers/java). **The business does not need to be in the same block as the user's location.**
6. After getting the score, you will choose the highest one and send it to the `ad-stream`. you need to have the following fields: `userId`, `storeId`, `name`. Here, `storeId` and `name` are the Yelp business id and name.



Information

| Price      | Device    | value |
| :--------- | :-------- | :---- |
| `$$$/$$$$` | iPhone XS | 3     |
| `$$`       | iPhone 7  | 2     |
| `$`        | iPhone 5  | 1     |
| (free)     |           | 0     |

**Table 12:** Price and device value table



### PreRequisites

1. Download the starter code for Task 3 on the master node.

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/kafka-samza-taxicabs/templates/ad-match.tgz
   ```

2. Untar the starter code tar file.

   ```
   tar xvfz ad-match.tgz
   ```

3. Run the `deploy_task3` script.

   ```
   cd ad-match && chmod +x deploy_task3 && ./deploy_task3
   ```

Once the download is complete, the data for the Kafka producer is available in `/home/hadoop/ad-match/trace_task3`. You will need to re-run the Producer code with the new tracefile. Copy this new tracefile to your student VM instance. You can run the following command in your master node to do this:

```
scp -i ~/<KEY_NAME>.pem /home/hadoop/ad-match/trace_task3 clouduser@<VM_PUBLIC_IP>:~/stream-processing/DataProducer/
```

### Steps to complete the task

1. Make necessary changes to the Producer code.
   - Update the trace file name to `trace_task3`
   - Do not send any messages to the `driver-location` topic since no information from driver's location is needed to calculate the ad-match score.
   - Make sure the events that are needed for updating the information in any key-value pair are sent to all 5 partitions and other events are sent to the partition based on the block ID.
2. Fill in the necessary internal IPs identified by the placeholder in `ad-match/src/main/config/ad-match.properties`. Keys that should be filled
   - `yarn.package.path`
   - `systems.kafka.consumer.zookeeper.connect`
   - `systems.kafka.producer.bootstrap.servers`
3. Add the necessary configuration and logic to calculate the ad-match score for each new client in the following files
   - `ad-match/src/main/java/com/cloudcomputing/samza/nycabs/AdMatchTask.java`
   - `ad-match/src/main/java/com/cloudcomputing/samza/nycabs/application/AdMatchTaskApplication.java`
4. Implement a function to process `RIDER_INTEREST` event. Check the duration field first, and **only** update the user's interest when the `duration` value is greater than **5 minutes**.
5. Implement a function to choose the right advertisement for the user in processing `RIDE_REQUEST`. After getting the highest score of business, send out the result in `ad-stream`.
6. You **do not** need to update the `travel_count` field in each user's profile after one travel.

#### How to submit

1. Export the required credentials on the Samza cluster master node

   ```
   export HISTIGNORE=export* # so that the following export commands will not be tracked into bash history
   export SUBMISSION_USERNAME=your_submission_username
   export SUBMISSION_PASSWORD=your_submission_pwd
   chmod +x submitter_task3
   ```

2. On the Samza cluster, run `submitter_task3` to make the submission. Running the submitter will generate the stream and validate the output stream. This may take a while since we will be sending a large number of events to your Samza job. The best advice to avoid long code-compile-test times is to constantly monitor the YARN application logs for any errors or failures.

3. On the Samza cluster, start the Samza job from another shell when prompted by the submitter. You can run your Samza job manually or use a script that we provided in `/home/hadoop/ad-match/ad-match/runner.sh` to compile, create the tar file, upload it to HDFS and run the job. This step is required because running the Task 3 submitter kills all the existing running jobs.

4. In the Producer code, replace the trace file name with the Task 3 tracefile. Start your Kafka producer from the when prompted by the submitter.

5. **Note:** Please note that the submitter will kill all the existing YARN applications, delete all Kafka topics you created, and create topics `events` with 5 partitions for you. If you use different names for the topics, the submitter will throw an exception because the topics it tries to delete do not exist.

#### Hints

1. If the submitter seems to hang for a long time, it is because there is nothing on the output stream (because of an error in the Samza job). Look at the YARN logs to find out what went wrong.
2. KeyValueStore will **not** be shared across partitions.
3. Please read the Samza documentation about [streaming](https://samza.apache.org/learn/documentation/versioned/container/streams.html) and [state management](https://samza.apache.org/learn/documentation/0.14/container/state-management.html) to learn more about Samza.
4. Make sure you calculate the distance with [this way](https://www.geodatasource.com/developers/java) (unit="M")
5. You could use `./start_kafka` if you want to restart Kafka.
6. You are not required to complete Task 2 to complete Task 3 since the grader will only check for the correctness.

Bonus Task

# Bonus task: Stream going through multiple transformations

## Introduction

In the real world, a stream would often go through multiple transformations to reach the desired final state. Some companies using this strategy include Slack and LinkedIn.

![Slack Architecture](https://samza.apache.org/img/latest/case-studies/slack-samza-pipeline.png)**Figure 5:** Slack Architecture (Reference: [Samza - Building streaming data pipelines for monitoring and analytics at Slack](https://samza.apache.org/case-studies/slack)).![LinkedIn Architecture](https://samza.apache.org/img/latest/case-studies/linkedin-atc-samza-pipeline.png)**Figure 6:** LinkedIn Architecture (Reference: [Samza - Air Traffic Controller with Samza at LinkedIn](https://samza.apache.org/case-studies/linkedin)).

We will continue using the NYCabs example to simulate such a case in the bonus task.

Businesses on Yelp pay a certain amount of money to an advertisement company to produce an advertisement for them. The Yelp businesses also pay NYCabs to display the advertisement on their cab service. The price is shown in `adPrice` field in `NYCstoreAds.json` (You can find this file under `src/main/resources` folder).

If a rider in a cab clicks the advertisement (i.e., a line with `"clicked": "true"` in the trace file), the advertisement company will get `80%` of the money paid by the Yelp business as a reward for its creativity and NYCabs will get the remaining `20%`. On the other hand, if a rider in a cab does not click the advertisement (i.e., a line with `"clicked": "false"` in the trace file), the advertisement company and NYCabs will split the money paid by the Yelp business.

The above scenario can be summarized as the below architecture:![Bonus Task](https://clouddeveloper.blob.core.windows.net/assets/kafka-samza-taxicabs/images/bonusTask.png)**Figure 7:** Bonus Task.

## Ad-price calculation logic

In this task, we will simplify the scenario to start working from `ad-click`. More specifically, you will write Samza code to consume the `ad-click` stream generated using the producer developed in Task 1, and output the client, Yelp business and advertisement price information to the `ad-price` stream.

| Field     | Type      | Values                                                   |
| :-------- | :-------- | :------------------------------------------------------- |
| `userId`  | `integer` | all possible non-negative integer as the ID of the rider |
| `storeId` | `string`  | random unique string for the business                    |
| `name`    | `string`  | business's name                                          |
| `clicked` | `string`  | `true`/`false`                                           |

**Table 13:** Input Stream (topic: ad-click) table





| Field     | Type      | Values                                                   |
| :-------- | :-------- | :------------------------------------------------------- |
| `userId`  | `integer` | all possible non-negative integer as the ID of the rider |
| `storeId` | `string`  | random unique string for the business                    |
| `ad`      | `integer` | the money distribute to the advertisement company        |
| `cab`     | `integer` | the money distribute to NYCabs                           |

**Table 14:** Output Stream (topic: ad-price) table



## PreRequisites

1. Download the starter code for the Bonus task on the master node.

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/kafka-samza-taxicabs/templates/ad-price.tgz
   ```

2. Untar the starter code tar file.

   ```
   tar xvfz ad-price.tgz
   ```

3. Run the `deploy_bonus` script.

   ```
   cd ad-price && chmod +x deploy_bonus && ./deploy_bonus
   ```

Once the download is complete, the data for the Kafka producer is available in `/home/hadoop/ad-price/trace_bonus`. You will need to re-run the Producer code with the new tracefile. Copy this new tracefile to your student VM instance. You can run the following command in your master node to do this:

```
scp -i ~/<KEY_NAME>.pem /home/hadoop/ad-price/trace_bonus clouduser@<VM_PUBLIC_IP>:~/stream-processing/DataProducer/
```

## Steps to complete the task

1. Update the Producer code to send all input data lines to `ad-click` stream. This topic has 5 partitions and is partitioned on the `userId` field.
2. Fill in the necessary internal IPs identified by the placeholder in `ad-price/src/main/config/ad-price.properties`. Keys that should be filled
   - `yarn.package.path`
   - `systems.kafka.consumer.zookeeper.connect`
   - `systems.kafka.producer.bootstrap.servers`
   - KeyValueStore (if any)
3. Add the necessary configuration and logic to calculate the ad-price for each new client in the following files
   - `ad-price/src/main/java/com/cloudcomputing/samza/nycabs/AdPriceTask.java`
   - `ad-price/src/main/java/com/cloudcomputing/samza/nycabs/application/AdPriceTaskApplication.java`
4. Implement a function to distribute the right amount of money to the advertisement company and the cab company. After that, send out the result in `ad-price`.
5. Implement your own test cases (at least 2) in `ad-price/src/test/java/com/cloudcomputing/samza/nycabs/TestAdPriceMatchTask.java`. Create `adClick.txt` in the resources for the test like in previous tasks. We will **manually grade** the test cases.

## How to submit

1. Export the required credentials on the Samza cluster master node

   ```
   export HISTIGNORE=export* # so that the following export commands will not be tracked into bash history
   export SUBMISSION_USERNAME=your_submission_username
   export SUBMISSION_PASSWORD=your_submission_pwd
   chmod +x submitter_bonus
   ```

2. On the Samza cluster, run `submitter_bonus` to make the submission. Running the submitter will generate the stream and validate the output stream. This may take a while since we will be sending a large number of events to your Samza job. The best advice to avoid long code-compile-test times is to constantly monitor the YARN application logs for any errors or failures.

3. On the Samza cluster, start the Samza job from another shell when prompted by the submitter. You can run your Samza job manually or use a script that we provided in `/home/hadoop/ad-price/ad-price/runner.sh` to compile, create the tar file, upload it to HDFS and run the job. This step is required because running the Bonus task submitter kills all the existing running jobs.

4. In the Producer code, replace the trace file name with the Bonus task tracefile. Start your Kafka producer from the when prompted by the submitter. This will begin producing messages to the `ad-click` stream,

## Hints

1. You can modify or skip running the unit test(s) for `DataProducer.java`.
2. You could use `./start_kafka` if you want to restart Kafka.

Optional Visualization Task

# Visualization (Optional)

Now that you are familiar with sending data to Kafka topics and using Samza jobs to process these streams, you want to share the results with your VCs in the hope that they will support your impending IPO. Your design team has developed a node.js application that leverages [deck.gl](https://github.com/uber/deck.gl/) and [react-map-gl](https://github.com/uber/react-map-gl) to render driver locations and trips. We provide a solution that populates the stream from the `driver-locations` topic. Additionally, you are free to read the `events` and `match-stream` to visualize rider events. You can run the visualization to see how the streams look.

## Visualization Setup

1. Make sure you created and populated the `driver-locations` topic using the `tracefile` from Task 1.

2. On the Student Instance, download and extract the application.

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/kafka-samza-taxicabs/templates/map_view.tgz
   
   tar -xvzf map_view.tgz
   
   cd map_view
   ```

3. Install the npm dependencies.

   ```
   npm install
   ```

4. You will need to replace the EMR master DNS with your cluster's in `map_view/src/server.js` at line 22.

5. Run the application.

   ```
   export MAPBOX_ACCESS_TOKEN="pk.eyJ1IjoiY2NtYXBkZW1vIiwiYSI6ImNqMGdhYm53MzAwMGYyd3Vqa3gwanhrb3oifQ.OcLKkjkGMvcE-RynMX7uFg"
   
   npm run start:dev:universal
   ```

6. Then you can enjoy the visualization experience on `http://[your-workspace-public-DNS]:3000`, that looks like: (Please wait a few minutes patiently before the visualization gets rendered.)

![New York City Taxi Cabs](https://clouddeveloper.blob.core.windows.net/assets/kafka-samza-taxicabs/gifs/map_render.gif)**Figure 8:** Node.js trip visualization.