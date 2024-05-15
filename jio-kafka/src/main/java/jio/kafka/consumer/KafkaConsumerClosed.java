package jio.kafka.consumer;

import java.util.Objects;
import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Label("Kafka_Consumer_Closed")
@Name("jio.kafka.consumer.closed")
@Category({"JIO", "KAFKA", "CONSUMER"})
@Description("Consumer closed")
@StackTrace(value = false)
class KafkaConsumerClosed extends Event {

  KafkaConsumerClosed(final String name) {
    this.name = Objects.requireNonNull(name);
  }

  private final String name;


}
