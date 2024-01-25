package jio.api;

import jio.IO;
import jio.ListExp;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

public class TestManagedBlocker {

  static Random random = new Random();

  public static int computation() {

    int r = random.nextInt(10);
    try {
      Thread.sleep(r);

    } catch (InterruptedException e) {
      Thread.currentThread()
            .interrupt();
      e.printStackTrace();
    }
    return r;
  }

  /**
   * RejectedExecutionException: Thread limit exceeded replacing blocked worker
   * {@link java.util.concurrent.ForkJoinPool#DEFAULT_COMMON_MAX_SPARES}
   */
  @Test
  public void testManagedBlockerLimits() {

    int MAX = 550;
    ListExp<Integer> exp = ListExp.par();

    for (int i = 0; i < MAX; i++) {

      exp = exp.append(IO.managedLazy(() -> computation())
                         .debug());

    }

    List<Integer> list = exp.join();

    System.out.println(list.stream()
                           .reduce(Integer::sum));


  }
}
