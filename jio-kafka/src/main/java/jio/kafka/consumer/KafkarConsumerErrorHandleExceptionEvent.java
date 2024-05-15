package jio.kafka.consumer;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Label("Kafka_Consumer_Error_Handler_Exception")
@Name("jio.kafka.consumer.error.handler.exception")
@Category({"JIO", "KAFKA", "CONSUMER"})
@Description("Error handler produces an exception")
@StackTrace(value = false)
class KafkarConsumerErrorHandleExceptionEvent extends Event {

  String name;

  String errorHandlerException;

  String originalException;

  KafkarConsumerErrorHandleExceptionEvent(final String name,
                                          final String errorHandlerException,
                                          final String originalException) {
    this.name = name;
    this.errorHandlerException = errorHandlerException;
    this.originalException = originalException;
  }
}
