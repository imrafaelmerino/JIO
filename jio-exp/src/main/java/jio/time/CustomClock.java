package jio.time;

import java.util.function.Supplier;

record CustomClock(Supplier<Long> time) implements Clock {

  @Override
  public Long get() {
    return time.get();
  }
}