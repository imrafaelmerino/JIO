package jio.test.pbt;

import jio.IO;
import jsonvalues.JsObj;


public interface Testable {

    default IO<Report> check() {
        return check(JsObj.empty());
    }

    IO<Report> check(JsObj conf);
}
