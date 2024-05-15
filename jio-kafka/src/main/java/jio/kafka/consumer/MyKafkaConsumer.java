package jio.kafka.consumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import jio.ExceptionFun;
import jio.IO;
import jio.ListExp;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

public final class MyKafkaConsumer<Key, Value> {

  private final KafkaConsumer<Key, Value> consumer;
  private final ExecutorService executorService;
  private final int instances;
  private final String name;

  private final AtomicBoolean closed = new AtomicBoolean(false);

  private final boolean enableJFR;


  public MyKafkaConsumer(final String name,
                         final ExecutorService executorService,
                         final Properties props,
                         final List<String> topics,
                         final int instances,
                         final boolean enableJFR
                        ) {
    if (instances <= 0) {
      throw new IllegalArgumentException("instances <=0");
    }
    this.name = name;
    this.instances = instances;
    this.consumer = new KafkaConsumer<>(Objects.requireNonNull(props));
    this.executorService = Objects.requireNonNull(executorService);
    consumer.subscribe(Objects.requireNonNull(topics));
    this.enableJFR = enableJFR;
  }


  public IO<Void> create(final Duration pollTimeout,
                         final BiConsumer<KafkaConsumer<Key, Value>, ConsumerRecords<Key, Value>> handler,
                         final BiConsumer<KafkaConsumer<Key, Value>, Exception> errorHandlers) {
    Objects.requireNonNull(pollTimeout);
    Objects.requireNonNull(handler);
    Objects.requireNonNull(errorHandlers);
    List<IO<Void>> consumers = new ArrayList<>();
    IO<Void> consumer =
        IO.effect(() -> executorService.submit(enableJFR ?
                                               jfrCallback(pollTimeout,
                                                           handler,
                                                           errorHandlers) :
                                               callback(pollTimeout,
                                                        handler,
                                                        errorHandlers)
                                              )
                 );
    if (instances == 1) {
      return consumer;
    }
    for (var i = 0; i < instances; i++) {
      consumers.add(consumer);
    }

    return ListExp.par(consumers)
                  .map(n -> null);
  }

  private Callable<Void> callback(Duration pollTimeout,
                                  BiConsumer<KafkaConsumer<Key, Value>, ConsumerRecords<Key, Value>> handler,
                                  BiConsumer<KafkaConsumer<Key, Value>, Exception> errorHandlers) {
    return () -> {
      while (!closed.get()) {
        try {
          handler.accept(this.consumer,
                         this.consumer.poll(pollTimeout)
                        );
        } catch (WakeupException wakeupException) {
          if (!closed.get()) {
            throw wakeupException;
          } else {
            consumer.close();
          }
          break;
        } catch (Exception handlerExc) {
          errorHandlers.accept(this.consumer,
                               handlerExc);
        }
      }

      return null;
    };

  }

  private Callable<Void> jfrCallback(Duration duration,
                                     BiConsumer<KafkaConsumer<Key, Value>, ConsumerRecords<Key, Value>> handler,
                                     BiConsumer<KafkaConsumer<Key, Value>, Exception> errorHandlers) {
    return () -> {
      while (!closed.get()) {
        KafkaConsumerEvent event = new KafkaConsumerEvent();
        try {
          event.begin();
          ConsumerRecords<Key, Value> records = consumer.poll(duration);
          handler.accept(consumer,
                         records
                        );
          if (event.shouldCommit()) {
            event.result = "SUCCESS";
            event.name = name;
            event.records = records.count();
            event.partitions = records.partitions()
                                      .size();
            event.commit();
          }
        } catch (WakeupException wakeupException) {
          if (!closed.get()) {
            throw wakeupException;
          } else {
            var consumerClosed = new KafkaConsumerClosed(name);
            consumerClosed.commit();
            consumer.close();
          }
          break;
        } catch (Exception handlerExc) {
          String exceptionDetail = ExceptionFun.findUltimateCause(handlerExc)
                                               .toString();
          try {
            event.result = "FAILURE";
            event.name = name;
            event.exception = exceptionDetail;
            event.commit();
            errorHandlers.accept(this.consumer,
                                 handlerExc);
          } catch (Exception errorHandlerExc) {
            var errorHandleExceptionEvent =
                new KafkarConsumerErrorHandleExceptionEvent(name,
                                                            ExceptionFun.findUltimateCause(errorHandlerExc)
                                                                        .toString(),
                                                            exceptionDetail
                );
            errorHandleExceptionEvent.commit();
          }
        }
      }
      return null;
    };
  }


  public void shutdown() {
    closed.set(true);
    consumer.wakeup();
    executorService.shutdown();
  }

}
