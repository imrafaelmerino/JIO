package jio.api;

import java.time.Duration;
import java.util.List;
import jio.RetryPolicies;
import jio.RetryStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PoliciesTest {

  @Test
  public void test_simulation_1() {

    List<RetryStatus> simulation = RetryPolicies.incrementalDelay(Duration.ofMillis(10))
                                                .limitRetriesByCumulativeDelay(Duration.ofMillis(120))
                                                .simulate(20);
    Assertions.assertEquals(5,
                            simulation.size()
                           );
    Assertions.assertEquals(new RetryStatus(4,
                                            Duration.ofMillis(100),
                                            Duration.ofMillis(40)
                            ),
                            simulation.getLast()
                           );

  }

  @Test
  public void test_simulation_2() {

    Duration base = Duration.ofMillis(10);
    Duration cap = Duration.ofMillis(100);
    List<RetryStatus> simulation = RetryPolicies.incrementalDelay(base)
                                                .capDelay(Duration.ofMillis(100))
                                                .simulate(20);
    Assertions.assertEquals(20,
                            simulation.size()
                           );
    Assertions.assertTrue(simulation.stream()
                                    .allMatch(rs -> rs.previousDelay()
                                                      .compareTo(cap) <= 0));

  }

  @Test
  public void test_simulation_3() {

    Duration acc = Duration.ofMillis(150);
    List<RetryStatus> simulation = RetryPolicies.incrementalDelay(Duration.ofMillis(10))
                                                .limitRetriesByCumulativeDelay(acc)
                                                .simulate(20);
    Assertions.assertEquals(6,
                            simulation.size()
                           );
    Assertions.assertTrue(simulation.stream()
                                    .allMatch(rs -> rs.cumulativeDelay()
                                                      .compareTo(acc) <= 0));

  }

  @Test
  public void test_simulation_4() {

    Duration base = Duration.ofMillis(10);
    Duration cap = Duration.ofMillis(100);
    List<RetryStatus> simulation = RetryPolicies.incrementalDelay(base)
                                                .limitRetriesByDelay(cap)
                                                .simulate(20);
    Assertions.assertEquals(9,
                            simulation.size()
                           );
    Assertions.assertTrue(simulation.stream()
                                    .allMatch(rs -> rs.previousDelay()
                                                      .compareTo(cap) <= 0));

  }

  @Test
  public void test_simulation_5() {
    Duration tenMillis = Duration.ofMillis(10);
    List<RetryStatus> simulation = RetryPolicies.incrementalDelay(tenMillis)
                                                .append(RetryPolicies.limitRetries(2))
                                                .followedBy(RetryPolicies.constantDelay(tenMillis)
                                                                         .limitRetriesByCumulativeDelay(Duration.ofMillis(200)))
                                                .simulate(20);

    System.out.println(simulation);
    Assertions.assertEquals(20,
                            simulation.size()
                           );
    Assertions.assertEquals(10,
                            simulation.get(1)
                                      .previousDelay()
                                      .toMillis()
                           );
    Assertions.assertEquals(20,
                            simulation.get(2)
                                      .previousDelay()
                                      .toMillis()
                           );
    Assertions.assertEquals(200,
                            simulation.getLast()
                                      .cumulativeDelay()
                                      .toMillis()
                           );
    Assertions.assertTrue(simulation.subList(3,
                                             simulation.size()
                                            )
                                    .stream()
                                    .allMatch(rs -> rs.previousDelay()
                                                      .compareTo(tenMillis) == 0));

  }

}
