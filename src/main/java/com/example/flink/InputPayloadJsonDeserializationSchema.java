package com.example.flink;

import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class InputPayloadJsonDeserializationSchema
    implements KafkaRecordDeserializationSchema<InputPayload> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<InputPayload> out)
      throws IOException {

    String recordValue = new String(record.value()); // Get the raw value as a string

    try {
      JsonNode jsonNode = objectMapper.readTree(recordValue);

      // Validate that all required fields are present
      if (jsonNode.has("name")
          && jsonNode.has("address")
          && jsonNode.has("dateOfBirth")
          && jsonNode.has("age")) {
        InputPayload inputPayload = new InputPayload();
        inputPayload.setName(jsonNode.get("name").asText());
        inputPayload.setAddress(jsonNode.get("address").asText());
        inputPayload.setDateOfBirth(jsonNode.get("dateOfBirth").asText());
        inputPayload.setAge(jsonNode.get("age").asInt());

        // Emit the valid record
        out.collect(inputPayload);
      } else {
        // Skip and log incomplete/malformed JSON
        System.out.println("Skipping invalid payload (missing fields): " + recordValue);
      }
    } catch (Exception e) {
      // Skip and log completely malformed JSON
      System.out.println("Skipping invalid payload (malformed JSON): " + recordValue);
    }
  }

  @Override
  public TypeInformation<InputPayload> getProducedType() {
    return TypeInformation.of(InputPayload.class);
  }
}
