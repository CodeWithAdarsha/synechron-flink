package com.example.flink;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class CsvRecordWriter extends RichSinkFunction<InputPayload> {

  private transient BufferedWriter writer;

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);

    Path outputDir = Paths.get("output");
    Files.createDirectories(outputDir); // This will create the directory if it doesn't exist

    // Create the file if it doesn't exist and open BufferedWriter
    Path filePath = outputDir.resolve("messages.csv");
    writer =
        Files.newBufferedWriter(
            filePath,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND);
  }

  @Override
  public void close() throws Exception {
    super.close();
    if (writer != null) {
      writer.close();
    }
  }

  @Override
  public void invoke(InputPayload value, Context context) throws Exception {
    String csvLine =
        value.getName()
            + ","
            + value.getAddress()
            + ","
            + value.getDateOfBirth()
            + ","
            + value.getAge();
    writer.write(csvLine);
    writer.write("\n");
  }
}
