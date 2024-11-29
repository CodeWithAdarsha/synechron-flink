package com.example.flink;

import javax.annotation.Nullable;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;

public class InputRecordSerializationSchema
    implements KafkaRecordSerializationSchema<InputPayload> {
  private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson for JSON serialization

  @Nullable
  @Override
  public ProducerRecord<byte[], byte[]> serialize(
      InputPayload element, KafkaSinkContext context, Long timestamp) {

    try {
      // Convert InputPayload to JSON byte array using Jackson
      byte[] payloadBytes = objectMapper.writeValueAsBytes(element);

      String topic = element.getAge() % 2 == 0 ? "EVEN_TOPIC" : "ODD_TOPIC";

      return new ProducerRecord<>(topic, null, payloadBytes);

    } catch (Exception e) {

    }
    return null;
  }
}
