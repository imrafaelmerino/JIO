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
 * Represents a command that allows users to execute interactive programs that send HTTP requests and receive HTTP
 * responses using a provided function. Users can specify the command name, description, and the function to build and
 * send the HTTP request, and the command can be executed as follows:
 * <p>
 * Usage: {@code http command_name [options]} - {@code command_name}: The name of the HTTP command to execute. -
 * {@code [options]}: Additional options or parameters for the HTTP request.
 * <p>
 * To get help and show the description of the HTTP command, users can enter: {@code help http command_name}
 * <p>
 * This command is designed for sending HTTP requests and displaying HTTP response details, including status code,
 * response time, HTTP method, URI, response body, and headers.
 */
public class HttpCommand extends Command {

    private static final String PREFIX_COMMAND = "http";
    private final BiFunction<JsObj, String[], IO<HttpResponse<String>>> request;

    /**
     * Constructs a new {@code HttpCommand} with the specified name, description, and HTTP request function.
     *
     * @param name        The name of the HTTP command.
     * @param description The description of the command (displayed in the help command).
     * @param req         The function that takes configuration, user input tokens, and builds the HTTP request,
     *                    returning an {@code IO} that represents the HTTP response.
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

    /**
     * Applies the HTTP command to execute an HTTP request with the given configuration and user input tokens.
     *
     * @param conf  The configuration for the HTTP request.
     * @param state The current state of the console.
     * @return A function that takes user input tokens and returns an {@code IO} representing the HTTP response.
     */
    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return tokens -> IO.lazy(Clock.realTime)
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
