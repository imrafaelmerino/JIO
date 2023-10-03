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
 * Represents an expression that is reduced to a JSON object. It follows the same recursive structure as a JSON object,
 * where each key is associated with an effect. This class provides two constructors: 'seq' and 'par', for creating
 * expressions that evaluate effects sequentially or in parallel, respectively.
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
     * Creates a expression that is evaluated to the empty JsObj
     */
    public static JsObjExp seq() {
        var obj = new JsObjExpSeq();
        obj.bindings = new HashMap<>();
        return obj;
    }

    /**
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Your code defines a static method seq that creates a JsObjExp expression with three key-value bindings, where
     * each key is associated with a corresponding effect.If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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
     * Creates a JsObjExp expression that evaluates effects in parallel and binds them to the specified keys in a JSON
     * object. If any of the effects fail, the entire expression fails.
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

    /**
     * Creates a new JsObjExp with the given effect associated with the specified key.
     *
     * @param key    the key for the JSON object entry
     * @param effect the effect to associate with the key
     * @return a new JsObjExp with the specified key-value pair
     */
    public abstract JsObjExp set(final String key,
                                 final IO<? extends JsValue> effect
                                );

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


    @Override
    public abstract JsObjExp retryEach(final Predicate<Throwable> predicate,
                                       final RetryPolicy policy
                                      );


    @Override
    public JsObjExp retryEach(final RetryPolicy policy) {
        return retryEach(e -> true, policy);
    }


    @Override
    public abstract JsObjExp debugEach(final EventBuilder<JsObj> messageBuilder
                                      );

    @Override
    public abstract JsObjExp debugEach(final String context);
}
