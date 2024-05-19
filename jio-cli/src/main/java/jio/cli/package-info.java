/**
 * The `jio.console` package provides classes and components to build interactive command-line console applications.
 * These applications allow users to input commands, execute them, and interact with the program in a text-based
 * interface. This package includes various utility classes and predefined commands to streamline the development of
 * console applications.
 *
 * <p><strong>Key Components:</strong></p>
 * <ul>
 * <li>{@link jio.cli.Console}: The central class that creates a Read, Eval, Print Loop (REPL) program. It accepts a
 * list
 * of user-defined commands and provides methods to execute them interactively. Predefined commands are also
 * available.</li>
 *
 * <li>{@link jio.cli.Command}: An interface that defines the structure of a console command. Developers can
 * implement this
 * interface to create custom commands for the console application.</li>
 *
 * <li>{@link jio.cli.State}: A class that represents the state of the console application. It manages variables,
 * command history,
 * and other state-related aspects of the application.</li>
 * </ul>
 *
 * <p><strong>Predefined Commands:</strong></p>
 * <p>The package includes several predefined commands that can be readily used in console applications. These commands
 * cover a range of common functionality, such as listing available commands, reading and setting variables, executing
 * the last command, displaying command history, and more.</p>
 *
 * <p><strong>Creating Custom Commands:</strong></p>
 * <p>To extend the functionality of your console application, you can create custom commands. Implement the
 * {@link jio.cli.Command}
 * interface to define the behavior of your command, and then add it to the list of user-defined commands in the
 * `Console`
 * constructor.</p>
 *
 * <p><strong>Execution Loop:</strong></p>
 * <p>The `Console` class provides an `eval` method that starts the execution loop. Users can input commands, and the
 * program
 * will evaluate, execute, and display the results in a loop until explicitly exited. The `eval` method also accepts a
 * configuration JSON object that can be passed to commands for configuration purposes.</p>
 *
 * <p>Developers can leverage the provided components and commands to create robust and user-friendly command-line
 * applications.</p>
 */
package jio.cli;
