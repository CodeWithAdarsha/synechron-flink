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
