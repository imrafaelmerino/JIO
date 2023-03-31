package jio.console;

import jio.IO;
import jio.time.Clock;
import jsonvalues.*;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to create different commands to execute interactive programs that send http request
 * given a provided function. The constructor takes three arguments: the command name and description,
 * and the function that send the request and returns the http response. To execute the command:
 *
 * <pre>
 *     req command_name
 * </pre>
 * <p>
 * To get some help about the program and show the description:
 * <pre>
 *     help req command_name
 * </pre>
 */
public class HttpCommand extends Command {

    private final BiFunction<JsObj, String[], IO<HttpResponse<String>>> request;
    private static final String PREFIX_COMMAND = "req";

    /**
     * Constructor to create a GenerateCommand
     *
     * @param name        the name of the command
     * @param description the description (will show up if the user types in the help command)
     * @param req         the function that takes the configuration, the array of tokens typed in by the user and build the request
     *                    , returning the http response
     * @see JsObjConsole
     */
    public HttpCommand(final String name,
                       final String description,
                       final BiFunction<JsObj, String[], IO<HttpResponse<String>>> req
                      ) {
        super(String.format("%s %s",
                            PREFIX_COMMAND,
                            name
                           ),
              description,
              tokens ->
                      tokens[0].equals(PREFIX_COMMAND)
                              && tokens.length > 1
                              && name.equalsIgnoreCase(Functions.joinTail(tokens))
             );
        this.request = req;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> IO.fromSupplier(Clock.realTime)
                           .then(tic -> request.apply(conf, tokens)
                                               .map(resp ->
                                                            JsObj.of("status_code",
                                                                     JsInt.of(resp.statusCode()),
                                                                     "time",
                                                                     JsLong.of(Duration.ofMillis(System.currentTimeMillis() - tic)
                                                                                       .toMillis()),
                                                                     "method",
                                                                     JsStr.of(resp.request().method()),
                                                                     "uri",
                                                                     JsStr.of(resp.request().uri().toString()),
                                                                     "body",
                                                                     JsStr.of(resp.body()),
                                                                     "headers",
                                                                     headers2Obj(resp.headers().map())
                                                                    )
                                                                 .toString()
                                                   ));
    }

    private JsObj headers2Obj(final Map<String, List<String>> map) {
        JsObj json = JsObj.empty();
        for (Map.Entry<String, List<String>> entry : map.entrySet())
            json = json.set(entry.getKey(),
                            JsArray.ofIterable(entry.getValue()
                                                    .stream()
                                                    .map(JsStr::of)
                                                    .collect(Collectors.toList()))
                           );
        return json;
    }
}
