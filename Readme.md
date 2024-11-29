
## Flink Kafka Integration Example

This project demonstrates a Flink streaming application that reads data from Kafka, processes it, and writes it back to Kafka topics (EVEN and ODD) based on certain conditions. It uses Kafka as a source and sink, and the application is set up using Docker Compose with Flink, Kafka, and Zookeeper.

## Application Flow

### Overview

The main logic of the Flink application (`AppDemo`) involves consuming messages from a Kafka topic (`INPUT_TOPIC`), processing the data, and then routing the data into two Kafka topics (`EVEN_TOPIC` and `ODD_TOPIC`) based on the `age` field in the payload. The application uses the following components:

- **Kafka Source**: Consumes messages from the `INPUT_TOPIC` Kafka topic.
- **Kafka Sink**: Produces messages to the `EVEN_TOPIC` and `ODD_TOPIC` based on the payload's `age` (even or odd).
- **CSV Writer**: Writes output records to CSV files.

```
### Code Overview

```java
package com.example.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class AppDemo {

  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create the Kafka Source (for input topic)
    KafkaSource<InputPayload> kafkaSource = KafkaSource.<InputPayload>builder()
                                                        .setBootstrapServers("localhost:9092")
                                                        .setTopics("INPUT_TOPIC")
                                                        .setGroupId("flink-consumer-group")
                                                        .setStartingOffsets(OffsetsInitializer.earliest()) // Can be changed to earliest()
                                                        .setDeserializer(new InputPayloadJsonDeserializationSchema())  // Use the custom deserializer
                                                        .setProperty("max.poll.interval.ms", "300000") // increase polling interval
                                                        .setProperty("session.timeout.ms", "30000") // session timeout
                                                        .setProperty("request.timeout.ms", "60000") // request timeout
                                                        .build();

    // Consume from Kafka source
    DataStream<InputPayload> inputStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

    // Kafka Sink (producers for EVEN and ODD topics)
    KafkaSink<InputPayload> kafkaSink = KafkaSink.<InputPayload>builder()
                                                 .setBootstrapServers("localhost:9092")
                                                 .setRecordSerializer(new InputRecordSerializationSchema()) // Use custom serializer for routing
                                                 .build();

    // Sink to Kafka, sending records to the correct topic based on age (even or odd)
    inputStream.sinkTo(kafkaSink);

    inputStream.addSink(new CsvRecordWriter());

    env.execute("Flink Kafka Even Odd Router");

  }
}
```

### Key Components:
- **KafkaSource**: The source connector to consume messages from the Kafka topic `INPUT_TOPIC`.
- **KafkaSink**: The sink connector to produce messages to `EVEN_TOPIC` and `ODD_TOPIC` based on the `age` field of the `InputPayload` object.
- **Custom Deserialization and Serialization**: We use custom serializers and deserializers to handle the data.
- **CSV Writer**: Writes the processed records to a CSV file.

## Setup Instructions

### 1. **Flink Maven Project Setup**

1. **Clone the repository** and navigate to the project folder.

   ```bash
   git clone https://github.com/your-repository/flink-kafka-example.git
   cd flink-kafka-example
   ```

2. **Maven Command to Build the JAR**:

   Run the following Maven command to compile the application and create the fat JAR (including all dependencies):

   ```bash
   mvn clean package
   ```

   This will generate a JAR file in the `target` folder. The JAR file will include all the dependencies necessary for running the Flink job.

### 2. **Docker Compose Setup**

To run Kafka, Zookeeper, and Flink in Docker, create a directory called `infra` in your project and add the `docker-compose.yml` file.

#### `docker-compose.yml`

```yaml
version: '3'
services:
  zookeeper:
    image: wurstmeister/zookeeper:3.4.10
    ports:
      - "2181:2181"
  
  kafka:
    image: wurstmeister/kafka:latest
    environment:
      KAFKA_ADVERTISED_LISTENERS: INSIDE://kafka:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL: PLAINTEXT
      KAFKA_LISTENER_NAMES: INSIDE
      KAFKA_LISTENERS: INSIDE://0.0.0.0:9093
      KAFKA_LISTENER_INTERFACES: INSIDE
      KAFKA_LISTENER_NAME_INSIDE_LISTENER: INSIDE
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_INTERNAL_PORT: 9093
    ports:
      - "9092:9092"
  
  jobmanager:
    image: apache/flink:latest
    ports:
      - "8081:8081"
    environment:
      - JOB_MANAGER_RPC_ADDRESS=jobmanager
    command: jobmanager

  taskmanager:
    image: apache/flink:latest
    environment:
      - JOB_MANAGER_RPC_ADDRESS=jobmanager
    depends_on:
      - jobmanager
    command: taskmanager
```

- `jobmanager`: This is the Flink JobManager.
- `taskmanager`: This is the Flink TaskManager.
- `kafka`: This is the Kafka broker.
- `zookeeper`: This is Zookeeper for Kafka.

Run the following command in the `infra` directory:

```bash
docker-compose up
```

This will bring up Zookeeper, Kafka, Flink JobManager, and Flink TaskManager.

### 3. **Kafka Topics Setup**

Before running the Flink application, make sure the Kafka topics (`INPUT_TOPIC`, `EVEN_TOPIC`, and `ODD_TOPIC`) are created. You can create them using the Kafka command-line tool.

```bash
docker exec -it <kafka-container-name> bash
kafka-topics.sh --create --topic INPUT_TOPIC --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics.sh --create --topic EVEN_TOPIC --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics.sh --create --topic ODD_TOPIC --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 4. **Run the Flink Application**

1. **VM Options**:
   When running the Flink application from the IDE (IntelliJ or similar), add the following VM option to ensure proper module access:

   ```
   --add-opens java.base/java.lang=ALL-UNNAMED
   ```

2. **Add Dependencies in IntelliJ**:
   In IntelliJ, make sure to add dependencies with `provided` scope in your `pom.xml`. These are dependencies that will be provided at runtime (such as Flink libraries).

   Example:

   ```xml
   <dependency>
       <groupId>org.apache.flink</groupId>
       <artifactId>flink-streaming-java_2.11</artifactId>
       <version>1.15.2</version>
       <scope>provided</scope>
   </dependency>
   ```

3. **Build the JAR**:
   Run the Maven build again to make sure the `provided` dependencies are included in the runtime classpath.

   ```bash
   mvn clean package
   ```

4. **Upload the JAR to Flink Web UI**:
    - Navigate to the Flink Web UI at `http://localhost:8081`.
    - Click on **"Submit New Job"** and select the JAR file generated by Maven.
    - Set the **entry class** to `com.example.flink.AppDemo`.
    - Submit the job.

### 5. **Monitoring the Job**

You can monitor the job’s progress and logs through the Flink dashboard at `http://localhost:8081`. This page will provide details on the job's execution, and you can check for any issues or logs from the application.

---

## Dependencies Versions

- **Flink Version**: `1.15.2`
- **Kafka Version**: `2.8.0`
- **Zookeeper Version**: `3.4.10`
- **Maven Shade Plugin**: `3.1.1`
- **Flink Kafka Connector Version**: `1.15.2`

---

## Troubleshooting

- **Kafka Connection Issues**: Ensure that the Kafka and Zookeeper services are properly running. If you cannot connect, check the `docker-compose` logs.
- **Flink Job Not Found**: Ensure that you have built the JAR correctly and uploaded the correct one to the Flink UI.
- **Application Errors**: Check the Flink logs and make sure the data in `INPUT_TOPIC` matches the expected format for the deserializer.

---

## Conclusion

This README provides a step-by-step guide to setting up a Flink job that reads from and writes to Kafka, including