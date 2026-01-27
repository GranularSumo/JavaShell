package org.example.commands;

import java.util.List;

import org.example.Evaluator.CommandResult;
import org.example.ShellContext;

public class Pwd implements CommandInterface {

  @Override
  public CommandResult execute(List<String> args, ShellContext shell) {
    shell.out().println(shell.getCwd());
    return CommandResult.continueWith(0);
  }
}
