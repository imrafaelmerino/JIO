package jio.test.pbt;

import jio.IO;
import jsonvalues.JsObj;


public interface Testable {

    /**
     * Executes the property test defined by this Testable instance.
     * The actual execution of the property test is deferred until the {@link IO#get()} or {@link IO#result()} method
     * is invoked on the returned IO monad.
     *
     * @return An IO monad representing the asynchronous execution of the property test. The result
     * of the test is encapsulated in a Report object.
     */
    default IO<Report> check() {
        return check(JsObj.empty());
    }

    /**
     * Executes the property test defined by this Property instance with the given configuration.
     * The actual execution of the property test is deferred until the {@link IO#get()} or {@link IO#result()} method
     * is invoked on the returned IO monad.
     *
     * @param conf The JSON configuration used for property testing. The configuration provides
     *             additional information or parameters needed for the property test.
     * @return An IO monad representing the asynchronous execution of the property test. The result
     * of the test is encapsulated in a Report object.
     */
    IO<Report> check(JsObj conf);
}
