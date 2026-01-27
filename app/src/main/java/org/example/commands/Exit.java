package org.example.commands;

import java.util.List;

import org.example.Evaluator.CommandResult;
import org.example.ShellContext;

public class Exit implements CommandInterface {
  @Override
  public CommandResult execute(List<String> args, ShellContext shell) {
    return CommandResult.exitWith(0);
  }
}
