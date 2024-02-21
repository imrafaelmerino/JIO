package jio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class VirtualThreadExecutor {

  static final ExecutorService INSTANCE = Executors.newVirtualThreadPerTaskExecutor();

}
