package examples;

import jio.console.*;
import jsonvalues.JsObj;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;
import jsonvalues.spec.JsSpecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Alexa {

  public static void main(String[] args) {
    List<Command> myCommnads = new ArrayList<>();
    JsObjConsole program = JsObjConsole.of("a",
                                           JsConsole.of(JsSpecs.integer()),
                                           "b",
                                           JsConsole.of(JsSpecs.str()),
                                           "c",
                                           JsConsole.of(JsSpecs.bool()),
                                           "d",
                                           JsConsole.of(JsSpecs.arrayOfStr())
    );
    Random random = new Random();
    myCommnads.add(new SupplierCommand("supplier",
                                       "prints a random number",
                                       () -> random.nextLong() + ""));

    myCommnads.add(new JsObjConsoleCommand("person",
                                           "Executes a program to compose a person Json",
                                           program
    )
    );
    myCommnads.add(new GenerateCommand("person",
                                       "Generates a person Json",
                                       JsObjGen.of("a",
                                                   JsIntGen.arbitrary(),
                                                   "b",
                                                   JsStrGen.alphabetic()
                                       )
                                               .map(JsObj::toString)
    ));

    Console alexa = new Console(myCommnads);
    alexa.eval(JsObj.empty());
  }

}
