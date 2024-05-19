package jio.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jio.IO;

/**
 * Represents the state of the console, including variables, command history, and command results. The state keeps track
 * of variables, lists, and maps, and maintains a history of executed commands and their results.
 */
public final class State {

  final List<IO<String>> historyCommands = new ArrayList<>();

  final List<String> historyResults = new ArrayList<>();
  /**
   * Map with variables and their string values. The special variable "OUTPUT" stores the result of the execution of the
   * last command.
   */
  public final Map<String, String> variables = new HashMap<>();

  /**
   * Gets the command action (JIO effect) at the specified index in the command history.
   *
   * @param index the index of the command in the history
   * @return the command action as a JIO effect
   */
  public IO<String> getHistoryCommand(int index) {
    return historyCommands.get(index);
  }

}
