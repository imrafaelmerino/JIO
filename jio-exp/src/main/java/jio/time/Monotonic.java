package jio.time;

final class Monotonic implements Clock {

  @Override
  public Long get() {
    return System.nanoTime();
  }
}
