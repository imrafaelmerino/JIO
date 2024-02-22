package jio.console;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.Json;
import jsonvalues.spec.JsParserException;

final class Functions {

  static final Function<String, Json<? extends Json<?>>> toJson = str -> {
    try {
      return JsObj.parse(str);
    } catch (JsParserException e) {
      try {
        return JsArray.parse(str);
      } catch (Exception ex) {
        throw new RuntimeException("A well-defined Json expected.");
      }
    }
  };
  static final Function<String[], List<String>> tail = tokens -> Arrays.stream(tokens)
                                                                       .toList()
                                                                       .subList(1,
                                                                                tokens.length);
  private Functions() {
  }

  static String joinTail(String[] tokens) {
    return String.join(" ",
                       tail.apply(tokens));
  }

  static String indent(final JsPath path) {
    return IntStream.range(0,
                           (int) Math.pow(2,
                                          path.size()
                                         )
                          )
                    .mapToObj(i -> " ")
                    .collect(Collectors.joining());
  }

}
