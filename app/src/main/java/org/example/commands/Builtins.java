package org.example.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Builtins {

  private final Map<String, CommandInterface> builtins = new HashMap<>();

  public Builtins() {
    builtins.put("exit", new Exit());
    builtins.put("echo", new Echo());
    builtins.put("pwd", new Pwd());
    builtins.put("type", new Type(builtins.keySet()));
    builtins.put("cd", new Cd());
  }

  public boolean isBuiltin(String name) {
    return builtins.keySet().contains(name);
  }

  public CommandInterface get(String name) {
    return builtins.get(name);
  }

  public List<String> getNameList() {
    return new ArrayList<>(builtins.keySet());
  }

}
