package examples;

import jio.cli.*;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;
import jsonvalues.spec.JsSpecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Alexa {

    public static void main(String[] args) {

        List<Command> myCommands = new ArrayList<>();
        JsObjConsole program = JsObjConsole.of("a",
                                               JsConsole.of(JsSpecs.integer()),
                                               "b",
                                               JsConsole.of(JsSpecs.str()),
                                               "c",
                                               JsConsole.of(JsSpecs.bool()),
                                               "d",
                                               JsConsole.of(JsSpecs.arrayOfStr()
                                                           )
                                              );
        Random random = new Random();
        myCommands.add(new SupplierCommand("print-random",
                                           "prints a random number",
                                           () -> STR."\{random.nextLong()}"));

        myCommands.add(new JsObjConsoleCommand("person",
                                               "Executes a program to compose a person Json",
                                               program
                       )
                      );
        myCommands.add(new GenerateCommand("person",
                                           "Generates a person Json",
                                           JsObjGen.of("a",
                                                       JsIntGen.arbitrary(),
                                                       "b",
                                                       JsStrGen.alphabetic()
                                                      )
                                                   .map(JsObj::toString)
        ));
        Console alexa = new Console(myCommands);
        alexa.eval(JsObj.empty()
                        .set("colors", JsObj.of("result", JsStr.of("\u001B[0;34m")))
                        .set("log_file_dir",
                             "/Users/rmerino/Projects/jio/jio-cli/"));
    }

}
