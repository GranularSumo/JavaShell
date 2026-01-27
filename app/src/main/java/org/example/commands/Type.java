package org.example.commands;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.Evaluator.CommandResult;
import org.example.Evaluator.CommandUtils;
import org.example.ShellContext;

public class Type implements CommandInterface {

  private Set<String> builtins;

  public Type(Set<String> builtins) {
    this.builtins = builtins;
  }

  @Override
  public CommandResult execute(List<String> args, ShellContext shell) {
    for (String arg : args) {
      if (builtins.contains(arg)) {
        shell.out().println(arg + " is a shell builtin");
      } else {

        Optional<String> filepath = CommandUtils.getCommandFilepath(arg);
        if (filepath.isEmpty()) {
          shell.err().println(arg + " not found");
        } else {
          shell.out().println(arg + " is " + filepath.get());
        }
      }
    }
    return CommandResult.continueWith(0);
  }

}
