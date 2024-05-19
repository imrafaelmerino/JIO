package jio.cli;

import java.util.function.Function;
import jio.IO;
import jsonvalues.JsObj;

/**
 * Command to read the content from a file with the command:
 * <pre>
 * file-read /path/to/file
 * </pre>
 * <p>
 * Users can specify the absolute path to the file they want to read, and the command will return the file's contents as
 * a string stored in the variable 'output'. If the file is not found, the command allows for multiple retries.
 * <p>
 * Examples:
 * <pre>
 * file-read /Users/username/json.txt
 * file-read $var
 * </pre>
 *
 * @see Command
 */
class PrintConfCommand extends Command {

  private static final String COMMAND_NAME = "print-conf";

  public PrintConfCommand() {
    super(COMMAND_NAME,
          """
                  Prints out the configuration file.
              
                  Usage:
                      print-conf
              """
         );
  }

  @Override

  public Function<String[], IO<String>> apply(final JsObj conf,
                                              final State state
                                             ) {
    return _ -> IO.succeed(conf.toString());
  }

}
