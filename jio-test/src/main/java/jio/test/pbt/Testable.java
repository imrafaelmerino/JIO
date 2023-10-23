package jio.test.pbt;

import jio.IO;
import jsonvalues.JsObj;


public abstract sealed class Testable permits ParProperty, Property, SeqProperty {


    IO<Report> createTask() {
        return createTask(JsObj.empty());
    }


    abstract IO<Report> createTask(JsObj conf);


    /**
     * Executes the property test defined by this Testable instance.
     * @return  The result of the test is encapsulated in a Report object.
     */
    public Report check() {
        return createTask().result();
    }

    /**
     * Executes the property test defined by this Property instance with the given configuration.
     *
     * @param conf The JSON configuration used for property testing. The configuration provides additional information
     *             or parameters needed for the property test.
     * @return  The result of the test is  encapsulated in a Report object.
     */
    public Report check(final JsObj conf) {
        return createTask(conf).result();
    }
}
