package jio.test.pbt;

import jio.cli.Command;
import jio.cli.Console;
import jsonvalues.JsObj;
import jsonvalues.spec.JsParserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The `PropertyConsole` class is responsible for managing and interacting with properties using a console interface. It
 * allows you to execute commands related to properties, such as reading and configuring them.
 *
 * <pre>
 * {code
 * public class TestProperties {
 *
 * {@literal @}Command
 * static Property prop1;
 *
 * {@literal @}Command
 * static Property prop2;
 *
 * public static void main(String[] args) throws IOException {
 * new PropertyConsole(List.of(TestProperties.class)).start(args);
 * }
 *
 * typing the command 'list prop' in the console will show the list of properties available
 * }
 *
 * </pre>
 *
 * @see Console
 * @see Property
 * @see Command
 */
public final class PropertyConsole {

  private final List<Class<?>> propertyClasses;

  /**
   * Constructs a `PropertyConsole` object with the specified list of property classes.
   *
   * @param propertyClasses A list of classes representing properties that can be managed through the console. Each
   *                        class should include static fields of type `Property` annotated with {@code @Command}.
   */
  public PropertyConsole(List<Class<?>> propertyClasses) {
    this.propertyClasses = Objects.requireNonNull(propertyClasses);
  }

  /**
   * Starts the Property Console and processes command-line arguments.
   *
   * @param args The command-line arguments passed to the application. It can optionally include the path to a
   *             configuration file as the first argument; any additional arguments will be ignored.
   * @throws IOException if there is an issue reading the configuration file.
   */
  public void start(final String[] args) throws IOException {
    Objects.requireNonNull(args);
    var properties = getPropertiesCommand();
    List<Command> commands = new ArrayList<>();
    for (Property<?> property : properties) {
      commands.add(PropertyCommand.of(property));
    }
    Console console = new Console(commands);

    if (args.length == 0) {
      console.eval(JsObj.empty());
    }
    if (args.length > 1) {
      System.out.println(
                         "Only an argument with the absolute path to the configuration file is required");
    }
    console.eval(getConf(args[0]));
  }

  private JsObj getConf(String path) throws IOException {
    Path file = Paths.get(path);
    if (!file.toFile()
             .exists()) {
      throw new IllegalArgumentException(String.format("The path %s doesn't exist",
                                                       path));
    }
    if (!file.toFile()
             .isFile()) {
      throw new IllegalArgumentException(String.format("The path %s is not a file",
                                                       path));
    }
    String conf = Files.readString(file);
    try {
      return JsObj.parse(conf);
    } catch (JsParserException e) {
      throw new IllegalArgumentException(
                                         String.format("The content of the file %s is not a Json object",
                                                       path));
    }

  }

  @SuppressWarnings("rawtypes")
  private List<Property> getPropertiesCommand() {
    return propertyClasses.stream()
                          .flatMap(it -> Arrays.stream(it.getDeclaredFields())
                                               .toList()
                                               .stream()
                                               .peek(f -> f.setAccessible(true))
                                               .filter(f -> f.getType()
                                                             .equals(Property.class)
                                                            && f.isAnnotationPresent(jio.test.pbt.Command.class))
                          )
                          .filter(f -> {
                            try {
                              //if f is not static it throws a NullPointerException
                              var unused = f.get(null);
                              return true;
                            } catch (Exception e) {
                              System.out.printf(
                                                "Property %s need to be static to be converted in a command callable from the console%n",
                                                f.getName());
                              return false;
                            }
                          })
                          .map(f -> {
                            try {
                              return f.get(null);
                            } catch (IllegalAccessException e) {
                              throw new RuntimeException(e);
                            }
                          })
                          .map(c -> ((Property) c))
                          .toList();
  }
}
