package org.example.commands;

import java.util.List;

import org.example.Evaluator.CommandResult;
import org.example.ShellContext;

public class Echo implements CommandInterface {

  @Override
  public CommandResult execute(List<String> args, ShellContext shell) {
    shell.out().println(String.join(" ", args));
    return CommandResult.continueWith(0);
  }

}
