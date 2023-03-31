package jio.console;

import jio.IO;
import jsonvalues.JsObj;
import jsonvalues.JsPath;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;


/**
 * Represents a {@link JsConsole console} program to compose a json object from the user inputs.
 * It has the same recursive structure as a Json object, which makes very easy to create
 * interactive programs to compose JsObj:
 *
 * <pre>
 *     {@code
 *
 *           JsObjConsole.of("a", JsConsole.of(JsSpecs.integer()),
 *                           "b", JsConsole.of(JsSpecs.str()),
 *                           "c", JsConsole.of(JsSpecs.bool()),
 *                           "d", JsConsole.of(JsSpecs.arrayOfStr())
 *                           );
 *     }
 *
 * </pre>
 * 
 * If the user introduces a value that is not valid according to the specified spec,
 * an error message will be prompted, and they'll have up to three retries to get it right
 */
public class JsObjConsole implements JsConsole<JsObj> {
    private final Map<String, JsConsole<?>> bindings = new LinkedHashMap<>();

    /**
     * static factory method to create a JsObjIO of sixteen mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @param key11 the eleventh key
     * @param program11  the program associated to the eleventh key
     * @param key12 the twelfth key
     * @param program12  the program associated to the twelfth key
     * @param key13 the thirteenth key
     * @param program13  the program associated to the thirteenth key
     * @param key14 the fourteenth key
     * @param program14  the program associated to the fourteenth key
     * @param key15 the fifteenth key
     * @param program15  the program associated to the fifteenth key
     * @param key16 the sixteenth key
     * @param program16  the program associated to the sixteenth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of fifteen mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @param key11 the eleventh key
     * @param program11  the program associated to the eleventh key
     * @param key12 the twelfth key
     * @param program12  the program associated to the twelfth key
     * @param key13 the thirteenth key
     * @param program13  the program associated to the thirteenth key
     * @param key14 the fourteenth key
     * @param program14  the program associated to the fourteenth key
     * @param key15 the fifteenth key
     * @param program15  the program associated to the fifteenth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of fourteen mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @param key11 the eleventh key
     * @param program11  the program associated to the eleventh key
     * @param key12 the twelfth key
     * @param program12  the program associated to the twelfth key
     * @param key13 the thirteenth key
     * @param program13  the program associated to the thirteenth key
     * @param key14 the fourteenth key
     * @param program14  the program associated to the fourteenth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of thirteen mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @param key11 the eleventh key
     * @param program11  the program associated to the eleventh key
     * @param key12 the twelfth key
     * @param program12  the program associated to the twelfth key
     * @param key13 the thirteenth key
     * @param program13  the program associated to the thirteenth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of twelve mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @param key11 the eleventh key
     * @param program11  the program associated to the eleventh key
     * @param key12 the twelfth key
     * @param program12  the program associated to the twelfth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of eleven mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @param key11 the eleventh key
     * @param program11  the program associated to the eleventh key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of ten mappings
     *
     * @param key1  the first key
     * @param program1   the program associated to the first key
     * @param key2  the second key
     * @param program2   the program associated to the second key
     * @param key3  the third key
     * @param program3   the program associated to the third key
     * @param key4  the forth key
     * @param program4   the program associated to the forth key
     * @param key5  the fifth key
     * @param program5   the program associated to the fifth key
     * @param key6  the sixth key
     * @param program6   the program associated to the sixth key
     * @param key7  the seventh key
     * @param program7   the program associated to the seventh key
     * @param key8  the eight key
     * @param program8   the program associated to the eight key
     * @param key9  the ninth key
     * @param program9   the program associated to the ninth key
     * @param key10 the tenth key
     * @param program10  the program associated to the tenth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of nine mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @param key4 the forth key
     * @param program4  the program associated to the forth key
     * @param key5 the fifth key
     * @param program5  the program associated to the fifth key
     * @param key6 the sixth key
     * @param program6  the program associated to the sixth key
     * @param key7 the seventh key
     * @param program7  the program associated to the seventh key
     * @param key8 the eight key
     * @param program8  the program associated to the eight key
     * @param key9 the ninth key
     * @param program9  the program associated to the ninth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of eight mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @param key4 the forth key
     * @param program4  the program associated to the forth key
     * @param key5 the fifth key
     * @param program5  the program associated to the fifth key
     * @param key6 the sixth key
     * @param program6  the program associated to the sixth key
     * @param key7 the seventh key
     * @param program7  the program associated to the seventh key
     * @param key8 the eight key
     * @param program8  the program associated to the eight key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of seven mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @param key4 the forth key
     * @param program4  the program associated to the forth key
     * @param key5 the fifth key
     * @param program5  the program associated to the fifth key
     * @param key6 the sixth key
     * @param program6  the program associated to the sixth key
     * @param key7 the seventh key
     * @param program7  the program associated to the seventh key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of six mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @param key4 the forth key
     * @param program4  the program associated to the forth key
     * @param key5 the fifth key
     * @param program5  the program associated to the fifth key
     * @param key6 the sixth key
     * @param program6  the program associated to the sixth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of five mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @param key4 the forth key
     * @param program4  the program associated to the forth key
     * @param key5 the fifth key
     * @param program5  the program associated to the fifth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of four mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @param key4 the forth key
     * @param program4  the program associated to the forth key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of three mappings
     *
     * @param key1 the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @param key3 the third key
     * @param program3  the program associated to the third key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of two mappings
     *
     * @param ke1  the first key
     * @param program1  the program associated to the first key
     * @param key2 the second key
     * @param program2  the program associated to the second key
     * @return a JsObjIO
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
     * static factory method to create a JsObjIO of one mapping
     *
     * @param key the key
     * @param program  the program associated to the key
     * @return a JsObjIO
     */
    public static JsObjConsole of(final String key,
                                  final JsConsole<?> program
                                 ) {
        var console = new JsObjConsole();
        console.bindings.put(requireNonNull(key),
                             requireNonNull(program)
                            );
        return console;

    }

    @Override
    public IO<JsObj> apply(final JsPath path) {
        requireNonNull(path);
        return IO.fromEffect(() ->
                         {
                             var result = CompletableFuture.completedFuture(JsObj.empty());
                             for (var entry : bindings.entrySet()) {
                                 var currentPath = path.append(JsPath.fromKey(entry.getKey()));
                                 var nextValue = entry.getValue();
                                 result = result
                                         .thenCombine(nextValue
                                                              .apply(currentPath)
                                                              .get(),
                                                      (obj, value) -> obj.set(entry.getKey(),
                                                                              value
                                                                             )
                                                     );
                             }
                             return result;
                         });
    }
}
