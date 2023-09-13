package jio;

import jsonvalues.JsObj;
import jsonvalues.JsValue;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Represents a expression that is reduced to a Json object. It has the same
 * recursive structure as a Json object. Each key has an associated effect.
 */
public abstract sealed class JsObjExp extends Exp<JsObj>
        permits JsObjExpPar, JsObjExpSeq {

    Map<String, IO<? extends JsValue>> bindings;

    JsObjExp(Map<String, IO<? extends JsValue>> bindings,
             Function<ExpEvent, BiConsumer<JsObj, Throwable>> logger
            ) {
        super(logger);
        this.bindings = bindings;
    }

    /**
     * creates a brand-new JsObjExp putting the given effect into the specified key
     *
     * @param key    the key
     * @param effect the effect
     * @return a new JsObjExp
     */
    public abstract JsObjExp set(final String key,
                                 final IO<? extends JsValue> effect
                                );

    /**
     * Creates a JsObjExp that is evaluated to the empty JsObj
     *
     * @return a JsObjExp
     */
    public static JsObjExp seq() {
        var obj = new JsObjExpSeq();
        obj.bindings = new HashMap<>();
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of one mapping
     *
     * @param key    the first key
     * @param effect the mapping associated to the first key
     * @return a JsObjExp
     */
    public static JsObjExp seq(final String key,
                               final IO<? extends JsValue> effect
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key),
                         requireNonNull(effect)
                        );
        return obj;
    }


    /**
     * static factory method to create a JsObjExp of two mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @return a JsObjExp
     */
    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        return obj;

    }

    /**
     * static factory method to create a JsObjExp of three mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of four mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4
                              ) {

        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of five mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of six mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of seven mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @param key7    the seventh key
     * @param effect7 the mapping associated to the seventh key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of eight mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @param key7    the seventh key
     * @param effect7 the mapping associated to the seventh key
     * @param key8    the eighth key
     * @param effect8 the mapping associated to the eighth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        return obj;

    }

    /**
     * static factory method to create a JsObjExp of nine mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @param key7    the seventh key
     * @param effect7 the mapping associated to the seventh key
     * @param key8    the eighth key
     * @param effect8 the mapping associated to the eighth key
     * @param key9    the ninth key
     * @param effect9 the mapping associated to the ninth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        return obj;

    }

    /**
     * static factory method to create a JsObjExp of ten mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the tenth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of eleven mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the tenth key
     * @param effect11 the mapping associated to the eleventh key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of twelve mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of thirteen mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key,
     * @param key13    the thirteenth key
     * @param effect13 the mapping associated to the thirteenth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12,
                               final String key13,
                               final IO<? extends JsValue> effect13
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        obj.bindings.put(requireNonNull(key13),
                         requireNonNull(effect13)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of fourteen mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key,
     * @param key13    the thirteenth key
     * @param effect13 the mapping associated to the thirteenth key
     * @param key14    the fourteenth key
     * @param effect14 the mapping associated to the fourteenth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12,
                               final String key13,
                               final IO<? extends JsValue> effect13,
                               final String key14,
                               final IO<? extends JsValue> effect14
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        obj.bindings.put(requireNonNull(key13),
                         requireNonNull(effect13)
                        );
        obj.bindings.put(requireNonNull(key14),
                         requireNonNull(effect14)
                        );
        return obj;

    }

    /**
     * static factory method to create a JsObjExp of fifteen mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key,
     * @param key13    the thirteenth key
     * @param effect13 the mapping associated to the thirteenth key
     * @param key14    the fourteenth key
     * @param effect14 the mapping associated to the fourteenth key
     * @param key15    the fifteenth key
     * @param effect15 the mapping associated to the fifteenth key
     * @return a JsObjExp
     */

    public static JsObjExp seq(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12,
                               final String key13,
                               final IO<? extends JsValue> effect13,
                               final String key14,
                               final IO<? extends JsValue> effect14,
                               final String key15,
                               final IO<? extends JsValue> effect15
                              ) {
        var obj = new JsObjExpSeq();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        obj.bindings.put(requireNonNull(key13),
                         requireNonNull(effect13)
                        );
        obj.bindings.put(requireNonNull(key14),
                         requireNonNull(effect14)
                        );
        obj.bindings.put(requireNonNull(key15),
                         requireNonNull(effect15)
                        );
        return obj;


    }

    /**
     * Creates a JsObjExp that is evaluated to the empty JsObj
     *
     * @return a JsObjExp
     */
    public static JsObjExp par() {
        var obj = new JsObjExpPar();
        obj.bindings = new HashMap<>();
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of one mapping
     *
     * @param key    the first key
     * @param effect the mapping associated to the first key
     * @return a JsObjExp
     */
    public static JsObjExp par(final String key,
                               final IO<? extends JsValue> effect
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key),
                         requireNonNull(effect)
                        );

        return obj;

    }


    /**
     * static factory method to create a JsObjExp of two mapping
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @return a JsObjExp
     */
    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );

        return obj;

    }

    /**
     * static factory method to create a JsObjExp of three mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );


        return obj;
    }

    /**
     * static factory method to create a JsObjExp of four mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );


        return obj;
    }

    /**
     * static factory method to create a JsObjExp of five mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );


        return obj;
    }

    /**
     * static factory method to create a JsObjExp of six mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );


        return obj;
    }

    /**
     * static factory method to create a JsObjExp of seven mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @param key7    the seventh key
     * @param effect7 the mapping associated to the seventh key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );


        return obj;
    }

    /**
     * static factory method to create a JsObjExp of eight mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @param key7    the seventh key
     * @param effect7 the mapping associated to the seventh key
     * @param key8    the eighth key
     * @param effect8 the mapping associated to the eighth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );

        return obj;
    }

    /**
     * static factory method to create a JsObjExp of nine mappings
     *
     * @param key1    the first key
     * @param effect1 the mapping associated to the first key
     * @param key2    the second key
     * @param effect2 the mapping associated to the second key
     * @param key3    the third key
     * @param effect3 the mapping associated to the third key
     * @param key4    the fourth key
     * @param effect4 the mapping associated to the fourth key
     * @param key5    the fifth key
     * @param effect5 the mapping associated to the fifth key
     * @param key6    the sixth key
     * @param effect6 the mapping associated to the sixth key
     * @param key7    the seventh key
     * @param effect7 the mapping associated to the seventh key
     * @param key8    the eighth key
     * @param effect8 the mapping associated to the eighth key
     * @param key9    the ninth key
     * @param effect9 the mapping associated to the ninth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );


        return obj;
    }

    /**
     * static factory method to create a JsObjExp of ten mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the tenth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );

        return obj;
    }

    /**
     * static factory method to create a JsObjExp of eleven mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the tenth key
     * @param effect11 the mapping associated to the eleventh key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );

        return obj;

    }

    /**
     * static factory method to create a JsObjExp of twelve mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );

        return obj;
    }

    /**
     * static factory method to create a JsObjExp of thirteen mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key,
     * @param key13    the thirteenth key
     * @param effect13 the mapping associated to the thirteenth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12,
                               final String key13,
                               final IO<? extends JsValue> effect13
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        obj.bindings.put(requireNonNull(key13),
                         requireNonNull(effect13)
                        );
        return obj;
    }

    /**
     * static factory method to create a JsObjExp of fourteen mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key,
     * @param key13    the thirteenth key
     * @param effect13 the mapping associated to the thirteenth key
     * @param key14    the fourteenth key
     * @param effect14 the mapping associated to the fourteenth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12,
                               final String key13,
                               final IO<? extends JsValue> effect13,
                               final String key14,
                               final IO<? extends JsValue> effect14
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        obj.bindings.put(requireNonNull(key13),
                         requireNonNull(effect13)
                        );
        obj.bindings.put(requireNonNull(key14),
                         requireNonNull(effect14)
                        );

        return obj;
    }

    /**
     * static factory method to create a JsObjExp of fifteen mappings
     *
     * @param key1     the first key
     * @param effect1  the mapping associated to the first key
     * @param key2     the second key
     * @param effect2  the mapping associated to the second key
     * @param key3     the third key
     * @param effect3  the mapping associated to the third key
     * @param key4     the fourth key
     * @param effect4  the mapping associated to the fourth key
     * @param key5     the fifth key
     * @param effect5  the mapping associated to the fifth key
     * @param key6     the sixth key
     * @param effect6  the mapping associated to the sixth key
     * @param key7     the seventh key
     * @param effect7  the mapping associated to the seventh key
     * @param key8     the eighth key
     * @param effect8  the mapping associated to the eighth key
     * @param key9     the ninth key
     * @param effect9  the mapping associated to the ninth key
     * @param key10    the tenth key
     * @param effect10 the mapping associated to the eleventh key
     * @param key11    the eleventh key
     * @param effect11 the mapping associated to the eleventh key
     * @param key12    the twelfth key
     * @param effect12 the mapping associated to the twelfth key,
     * @param key13    the thirteenth key
     * @param effect13 the mapping associated to the thirteenth key
     * @param key14    the fourteenth key
     * @param effect14 the mapping associated to the fourteenth key
     * @param key15    the fifteenth key
     * @param effect15 the mapping associated to the fifteenth key
     * @return a JsObjExp
     */

    public static JsObjExp par(final String key1,
                               final IO<? extends JsValue> effect1,
                               final String key2,
                               final IO<? extends JsValue> effect2,
                               final String key3,
                               final IO<? extends JsValue> effect3,
                               final String key4,
                               final IO<? extends JsValue> effect4,
                               final String key5,
                               final IO<? extends JsValue> effect5,
                               final String key6,
                               final IO<? extends JsValue> effect6,
                               final String key7,
                               final IO<? extends JsValue> effect7,
                               final String key8,
                               final IO<? extends JsValue> effect8,
                               final String key9,
                               final IO<? extends JsValue> effect9,
                               final String key10,
                               final IO<? extends JsValue> effect10,
                               final String key11,
                               final IO<? extends JsValue> effect11,
                               final String key12,
                               final IO<? extends JsValue> effect12,
                               final String key13,
                               final IO<? extends JsValue> effect13,
                               final String key14,
                               final IO<? extends JsValue> effect14,
                               final String key15,
                               final IO<? extends JsValue> effect15
                              ) {
        var obj = new JsObjExpPar();
        obj.bindings.put(requireNonNull(key1),
                         requireNonNull(effect1)
                        );
        obj.bindings.put(requireNonNull(key2),
                         requireNonNull(effect2)
                        );
        obj.bindings.put(requireNonNull(key3),
                         requireNonNull(effect3)
                        );
        obj.bindings.put(requireNonNull(key4),
                         requireNonNull(effect4)
                        );
        obj.bindings.put(requireNonNull(key5),
                         requireNonNull(effect5)
                        );
        obj.bindings.put(requireNonNull(key6),
                         requireNonNull(effect6)
                        );
        obj.bindings.put(requireNonNull(key7),
                         requireNonNull(effect7)
                        );
        obj.bindings.put(requireNonNull(key8),
                         requireNonNull(effect8)
                        );
        obj.bindings.put(requireNonNull(key9),
                         requireNonNull(effect9)
                        );
        obj.bindings.put(requireNonNull(key10),
                         requireNonNull(effect10)
                        );
        obj.bindings.put(requireNonNull(key11),
                         requireNonNull(effect11)
                        );
        obj.bindings.put(requireNonNull(key12),
                         requireNonNull(effect12)
                        );
        obj.bindings.put(requireNonNull(key13),
                         requireNonNull(effect13)
                        );
        obj.bindings.put(requireNonNull(key14),
                         requireNonNull(effect14)
                        );
        obj.bindings.put(requireNonNull(key15),
                         requireNonNull(effect15)
                        );
        return obj;


    }

    Map<String, IO<? extends JsValue>> debugJsObj(Map<String, IO<? extends JsValue>> bindings,
                                                  String context
                                                 ) {
        return bindings.entrySet()
                       .stream()
                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                 e -> LoggerHelper.debugIO(e.getValue(),
                                                                           String.format("%s[%s]",
                                                                                         this.getClass().getSimpleName(),
                                                                                         e.getKey()
                                                                                        ),
                                                                           context
                                                                          )
                                                )
                               );
    }

    /**
     * Creates a new JsObjExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception is tested true against the specified predicate.
     *
     * @param predicate the predicate to test exceptions
     * @param policy    the retry policy
     * @return a new JsObjExp
     */
    @Override
    public abstract JsObjExp retryEach(final Predicate<Throwable> predicate,
                                       final RetryPolicy policy
                                      );

    /**
     * Creates a new JsObjExp expression where the given retry policy is applied recursively
     * to every subexpression when an exception happens.
     *
     * @param policy the retry policy
     * @return a new JsObjExp
     */
    @Override
    public JsObjExp retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


    /**
     * Creates a new JsObjExp that will write to the given logger information about every
     * computation evaluated to reduce this expression (like {@link #debugEach(String)} does).
     * A final log message created with the specified messageBuilder is written after reducing
     * the whole expression
     *
     * @param messageBuilder the builder to create the log message from the result of the expression
     * @return a new JsObjExp
     * @see #debugEach(String) debugEach
     */
    @Override
    public abstract JsObjExp debugEach(final EventBuilder<JsObj> messageBuilder
                                      );

    /**
     * Creates a new JsObjExp that will print out on the console information about every
     * computation evaluated to reduce this expression. The given context will be associated
     * to every subexpression and printed out to correlate all the evaluations (contextual
     * logging).
     * <p>
     * The line format is the following:
     * <p>
     * datetime thread logger [context] elapsed_time success|exception expression|subexpression result?
     * <p>
     * Find bellow an example:
     *
     * <pre>
     * {@code
     * JsObjExp.par("a", JsObjExp.par("a",A_AFTER_1_SEC.get().map(JsStr::of),
     *                                 "b", B_AFTER_1_SEC.get().map(JsStr::of)),
     *               "b", JsArrayExp.par(A_AFTER_1_SEC.get().map(JsStr::of),
     *                                   B_AFTER_1_SEC.get().map(JsStr::of)
     *                                   )
     *              )
     *            .debugEach("context")
     *            .join();
     * }
     * </pre>
     * <p>
     * 2023-02-04T16:06:43.72255+01:00 pool-1-thread-1 DEBUGGER [context] 1021998500 success JsObjExpPar[a]
     * 2023-02-04T16:06:43.722561+01:00 pool-2-thread-1 DEBUGGER [context] 1020132625 success JsObjExpPar[b]
     * 2023-02-04T16:06:43.723208+01:00 pool-2-thread-2 DEBUGGER [stub, call_counter=1] 1005051375 success b
     * 2023-02-04T16:06:43.723376+01:00 pool-1-thread-2 DEBUGGER [context] 1005418041 success JsArrayExpPar[0]
     * 2023-02-04T16:06:43.723599+01:00 pool-2-thread-2 DEBUGGER [context] 1005364333 success JsArrayExpPar[1]
     * 2023-02-04T16:06:43.732701+01:00 pool-1-thread-2 DEBUGGER [context] 1029336958 success JsObjExpPar[b]
     * 2023-02-04T16:06:43.737463+01:00 pool-2-thread-1 DEBUGGER [context] 1036923958 success JsObjExpPar[a]
     * 2023-02-04T16:06:43.737682+01:00 pool-2-thread-1 DEBUGGER [context] 1049685834 success JsObjExpPar {"a":{"a":"a","b":"b"},"b":["a","b"]}
     *
     * @param context the context shared by all the subexpressions that will be printed out
     * @return a new JsObjExp
     */
    @Override
    public abstract JsObjExp debugEach(final String context);
}
