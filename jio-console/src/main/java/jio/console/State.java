package jio.console;

import jio.IO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {

    /**
     * map with variables and their string values. The especial variable OUTPUT stores the result
     * of the execution of the last command
     */
    public Map<String, String> stringVariables = new HashMap<>();

    /**
     * map with variables and their associated list. Some commands need to store values in a list
     */
    public Map<String,List<String>> listsVariables = new HashMap<>();

    /**
     * map with variables and their associated maps. Some commands need to store values in a map
     */
    public Map<String,Map<String,String>> mapVariables = new HashMap<>();;

    final List<IO<String>> historyCommands = new ArrayList<>();

    final List<String> historyResults = new ArrayList<>();

    public IO<String> getHistoryCommand(int index) {
        return historyCommands.get(index);
    }

}
