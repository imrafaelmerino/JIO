package jio.jdbc;

import java.util.concurrent.atomic.AtomicLong;

final class EventCounter {

  private EventCounter(){}

   static final AtomicLong COUNTER = new AtomicLong(0);

}
