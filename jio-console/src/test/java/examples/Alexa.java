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

        ListExp.seq(ASK_FOR_INPUT(new AskForInputParams("Number of threads",
                                                        i -> i.chars().allMatch(Character::isDigit),
                                                        "Integer (0,1000]",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Duration (sg)",
                                                        i -> i.chars().allMatch(Character::isDigit),
                                                        "Integer (0,1000]",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Ramp up (sg)",
                                                        i -> i.chars().allMatch(Character::isDigit),
                                                        "Integer (0,1000]",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Master HOST",
                                                        i -> !i.isEmpty() && !i.isBlank(),
                                                        "No puede ser vacio",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Slave Hosts (slave1, slave2, ....)",
                                                        i -> Pattern.compile("^").matcher(i).matches(),
                                                        "No puede ser vacio",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Environment",
                                                        i -> !i.isEmpty() && !i.isBlank(),
                                                        "No puede ser vacio",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Test Name",
                                                        i -> !i.isEmpty() && !i.isBlank(),
                                                        "No puede ser vacio",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Application name",
                                                        i -> !i.isEmpty() && !i.isBlank(),
                                                        "No puede ser vacio",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 ),
                    ASK_FOR_INPUT(new AskForInputParams("Application version",
                                                        i -> !i.isEmpty() && !i.isBlank(),
                                                        "No puede ser vacio",
                                                        RetryPolicies.limitRetries(3)
                    )),
                    ASK_FOR_INPUT(new AskForInputParams("JMETER file (file with extension jmx)",
                                                        i -> Files.exists(Paths.get(i)) && Paths.get(i).toFile().isFile(),
                                                        "File doesn't exist",
                                                        RetryPolicies.limitRetries(3)
                    )),
                    ASK_FOR_INPUT(new AskForInputParams("Property file",
                                                        i -> Files.exists(Paths.get(i)) && Paths.get(i).toFile().isFile(),
                                                        "File doesn't exist",
                                                        RetryPolicies.limitRetries(3)
                    )),
                    ASK_FOR_INPUT(new AskForInputParams("Test Label (additional information to give some context)",
                                                        i -> !i.isEmpty() && !i.isBlank(),
                                                        "File doesn't exist",
                                                        RetryPolicies.limitRetries(3)
                                  )
                                 )
                   ).map(list -> list.stream()
                                     .map(it -> "\"" + it + "\"")
                                     .toList())
               .join()
               .stream()
               .collect(Collectors.joining("(", ")", " "));


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
                                    (JsObj o) -> o.isNotEmpty() ? TestResult.SUCCESS : TestFailure.reason("empty json")
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
