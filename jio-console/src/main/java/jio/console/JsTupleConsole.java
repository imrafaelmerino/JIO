package jio.console;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import jio.IO;
import jsonvalues.JsArray;
import jsonvalues.JsPath;

/**
 * Represents a {@link JsConsole console} program to compose a json array from the user inputs. It has the same
 * recursive structure as a json array, which makes very easy to create interactive programs to compose JsArray:
 *
 * <pre>
 * {@code
 *
 *        JsTupleConsole.of(JsConsole.of(JsSpecs.integer()),
 *                          JsConsole.of(JsSpecs.str())
 *                         );
 *     }
 *
 * </pre>
 * <p>
 * If the user introduces a value that is not valid according to the specified spec, an error message will be prompted,
 * and they'll have up to three retries to get it right
 */
public class JsTupleConsole implements JsConsole<JsArray> {

  private final List<JsConsole<?>> seq = new ArrayList<>();

  /**
   * static factory method to create a JsArrayIO
   *
   * @param head the head
   * @param tail the tail
   * @return a JsArrayIO
   */
  public static JsTupleConsole of(final JsConsole<?> head,
                                  final JsConsole<?>... tail
                                 ) {
    var array = new JsTupleConsole();
    array.seq.add(requireNonNull(head));
    array.seq.addAll(Arrays.asList(requireNonNull(tail)));
    return array;
  }

  /**
   * @param path the parent path of the array
   * @return a JsFuture that wen completed will return JsArray
   */
  @Override
  public IO<JsArray> apply(final JsPath path) {
    requireNonNull(path);
    return IO.effect(() -> {
      var result = CompletableFuture.completedFuture(JsArray.empty());
      for (int i = 0; i < seq.size(); i++) {
        var p = path.index(i);
        var io = seq.get(i);
        result = result.thenCombine(io.apply(p)
                                      .get(),
                                    JsArray::append
                                   );
      }

      return result;
    });
  }

}
