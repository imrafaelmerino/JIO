package jio.console;

import jio.IO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class State {

    Map<String, String> variables = new HashMap<>();

    public final List<IO<String>> historyCommands = new ArrayList<>();

    public final List<String> historyResults = new ArrayList<>();

    public IO<String> getHistoryCommand(int index) {
        return historyCommands.get(index);
    }

}
