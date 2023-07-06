package examples;

import fun.gen.Combinators;
import fun.gen.Gen;
import fun.tuple.Pair;
import jio.ListExp;
import jio.RetryPolicies;
import jio.console.*;
import jio.console.Programs.AskForInputParams;
import jio.pbt.Property;
import jio.pbt.TestFailure;
import jio.pbt.TestResult;
import jsonvalues.JsObj;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;
import jsonvalues.spec.JsSpecs;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static jio.console.Programs.ASK_FOR_INPUT;

public class Alexa {


    public static void main(String[] args) {
        ArrayList<Command> myCommnads = new ArrayList<>();
        JsObjConsole program =
                JsObjConsole.of("a", JsConsole.of(JsSpecs.integer()),
                                "b", JsConsole.of(JsSpecs.str()),
                                "c", JsConsole.of(JsSpecs.bool()),
                                "d", JsConsole.of(JsSpecs.arrayOfStr())
                               );
        Random random = new Random();
        myCommnads.add(new SupplierCommand("supplier","prints a random number",()-> random.nextLong()+""));

        myCommnads.add(new JsObjConsoleCommand("person",
                                               "Executes a program to compose a person Json",
                                               program
                       )
                      );
        myCommnads.add(new GenerateCommand("person", "Generates a person Json",
                                           JsObjGen.of("a", JsIntGen.arbitrary(),
                                                       "b", JsStrGen.alphabetic()
                                                      )
                                                   .map(JsObj::toString)
        ));


        myCommnads.add(new PropertyCommand(
                Property.ofFunction("example",
                                    (JsObj o) -> o.isNotEmpty() ?
                                            TestResult.SUCCESS :
                                            TestFailure.reason("empty json")
                                   )
                        .withDescription("Json empty")
                        .withGen(Combinators.freq(Pair.of(3, JsObjGen.of("a", JsStrGen.alphabetic())),
                                                  Pair.of(1, Gen.cons(JsObj.empty()))
                                                 )
                                )
        ));
        Console alexa = new Console(myCommnads);
        alexa.eval(JsObj.empty());
    }

}
