package jio.cli;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import jio.IO;
import jio.Result;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.JsValue;

/**
 * Represents a {@link JsConsole console} program to compose a JSON object from the user inputs. It has the same
 * recursive structure as a JSON object, which makes it very easy to create interactive programs to compose a
 * {@link jsonvalues.JsObj JsObj}:
 *
 * <pre>
 * {@code
 *           JsObjConsole.of("a", JsConsole.of(JsSpecs.integer()),
 *                           "b", JsConsole.of(JsSpecs.str()),
 *                           "c", JsConsole.of(JsSpecs.bool()),
 *                           "d", JsConsole.of(JsSpecs.arrayOfStr())
 *                           );
 *     }
 * </pre>
 * <p>
 * If the user introduces a output that is not valid according to the specified spec, an error message will be prompted,
 * and they'll have up to three retries to get it right.
 */
public class JsObjConsole implements JsConsole<JsObj> {

  private final Map<String, JsConsole<?>> bindings;

  JsObjConsole(Map<String, JsConsole<?>> bindings) {
    this.bindings = bindings;
  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10,
                                final String key11,
                                final JsConsole<?> program11,
                                final String key12,
                                final JsConsole<?> program12,
                                final String key13,
                                final JsConsole<?> program13,
                                final String key14,
                                final JsConsole<?> program14,
                                final String key15,
                                final JsConsole<?> program15,
                                final String key16,
                                final JsConsole<?> program16
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9,
                                  key10,
                                  program10,
                                  key11,
                                  program11,
                                  key12,
                                  program12,
                                  key13,
                                  program13,
                                  key14,
                                  program14,
                                  key15,
                                  program15
    );

    console.bindings.put(requireNonNull(key16),
                         requireNonNull(program16)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10,
                                final String key11,
                                final JsConsole<?> program11,
                                final String key12,
                                final JsConsole<?> program12,
                                final String key13,
                                final JsConsole<?> program13,
                                final String key14,
                                final JsConsole<?> program14,
                                final String key15,
                                final JsConsole<?> program15
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9,
                                  key10,
                                  program10,
                                  key11,
                                  program11,
                                  key12,
                                  program12,
                                  key13,
                                  program13,
                                  key14,
                                  program14
    );

    console.bindings.put(requireNonNull(key15),
                         requireNonNull(program15)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10,
                                final String key11,
                                final JsConsole<?> program11,
                                final String key12,
                                final JsConsole<?> program12,
                                final String key13,
                                final JsConsole<?> program13,
                                final String key14,
                                final JsConsole<?> program14
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9,
                                  key10,
                                  program10,
                                  key11,
                                  program11,
                                  key12,
                                  program12,
                                  key13,
                                  program13
    );

    console.bindings.put(requireNonNull(key14),
                         requireNonNull(program14)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10,
                                final String key11,
                                final JsConsole<?> program11,
                                final String key12,
                                final JsConsole<?> program12,
                                final String key13,
                                final JsConsole<?> program13
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9,
                                  key10,
                                  program10,
                                  key11,
                                  program11,
                                  key12,
                                  program12
    );

    console.bindings.put(requireNonNull(key13),
                         requireNonNull(program13)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10,
                                final String key11,
                                final JsConsole<?> program11,
                                final String key12,
                                final JsConsole<?> program12
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9,
                                  key10,
                                  program10,
                                  key11,
                                  program11
    );

    console.bindings.put(requireNonNull(key12),
                         requireNonNull(program12)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10,
                                final String key11,
                                final JsConsole<?> program11
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9,
                                  key10,
                                  program10
    );

    console.bindings.put(requireNonNull(key11),
                         requireNonNull(program11)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9,
                                final String key10,
                                final JsConsole<?> program10
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8,
                                  key9,
                                  program9
    );

    console.bindings.put(requireNonNull(key10),
                         requireNonNull(program10)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8,
                                final String key9,
                                final JsConsole<?> program9
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7,
                                  key8,
                                  program8
    );

    console.bindings.put(requireNonNull(key9),
                         requireNonNull(program9)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7,
                                final String key8,
                                final JsConsole<?> program8
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6,
                                  key7,
                                  program7
    );

    console.bindings.put(requireNonNull(key8),
                         requireNonNull(program8)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6,
                                final String key7,
                                final JsConsole<?> program7
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5,
                                  key6,
                                  program6
    );

    console.bindings.put(requireNonNull(key7),
                         requireNonNull(program7)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */

  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5,
                                final String key6,
                                final JsConsole<?> program6
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4,
                                  key5,
                                  program5
    );

    console.bindings.put(requireNonNull(key6),
                         requireNonNull(program6)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */

  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4,
                                final String key5,
                                final JsConsole<?> program5
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3,
                                  key4,
                                  program4
    );

    console.bindings.put(requireNonNull(key5),
                         requireNonNull(program5)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3,
                                final String key4,
                                final JsConsole<?> program4
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2,
                                  key3,
                                  program3
    );

    console.bindings.put(requireNonNull(key4),
                         requireNonNull(program4)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2,
                                final String key3,
                                final JsConsole<?> program3
  ) {

    var console = JsObjConsole.of(key1,
                                  program1,
                                  key2,
                                  program2
    );

    console.bindings.put(requireNonNull(key3),
                         requireNonNull(program3)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String ke1,
                                final JsConsole<?> program1,
                                final String key2,
                                final JsConsole<?> program2
  ) {

    var console = JsObjConsole.of(ke1,
                                  program1
    );

    console.bindings.put(requireNonNull(key2),
                         requireNonNull(program2)
    );

    return console;

  }

  /**
   * Static factory method to create a {@link JsObjConsole}
   */
  public static JsObjConsole of(final String key,
                                final JsConsole<?> program
  ) {
    var console = new JsObjConsole(new LinkedHashMap<>());
    console.bindings.put(requireNonNull(key),
                         requireNonNull(program)
    );
    return console;

  }

  /**
   * Sets a key-program mapping in this {@link JsObjConsole}.
   *
   * @param key     The key to set in the JSON object.
   * @param program The program associated with the key.
   * @return A new {@link JsObjConsole} instance with the specified key-program mapping added.
   */
  public JsObjConsole set(String key,
                          JsConsole<?> program) {
    Map<String, JsConsole<?>> map = new LinkedHashMap<>(bindings);
    map.put(key,
            program);
    return new JsObjConsole(map);
  }

  /**
   * Apply the JsObjConsole program to build a JSON object interactively.
   *
   * @param path The current JsPath for composing the JSON object.
   * @return An IO effect representing the completion of the JSON object.
   */
  @Override
  public IO<JsObj> apply(final JsPath path) {
    requireNonNull(path);
    return IO.task(() -> {
      var result = JsObj.empty();
      for (var entry : bindings.entrySet()) {
        var currentPath = path.append(JsPath.fromKey(entry.getKey()));
        var nextValue = entry.getValue();
        var r = nextValue.apply(currentPath)
                         .compute();
        switch (r) {
          case Result.Success(JsValue value) -> result = result.set(entry.getKey(),
                                                                    value
          );

          case Result.Failure(Exception exception) -> throw exception;
        }
      }
      return result;
    });
  }
}
