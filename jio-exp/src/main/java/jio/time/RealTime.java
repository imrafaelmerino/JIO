package jio.time;

final class RealTime implements Clock {

  @Override
  public Long get() {
    return System.currentTimeMillis();
  }
}