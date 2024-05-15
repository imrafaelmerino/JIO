package jio.kafka.consumer;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Label("Kafka_Consumer")
@Name("jio.kafka.consumer")
@Category({"JIO", "KAFKA", "CONSUMER"})
@Description("Consumer poll operation plus processing")
@StackTrace(value = false)
class KafkaConsumerEvent extends Event {

  String name;
  String result;

  //Can be an exception while pooling or processing
  String exception;

  int records;

  int partitions;

}
