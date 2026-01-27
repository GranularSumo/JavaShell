package org.example.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.example.Evaluator.CommandResult;
import org.example.ShellContext;

public class Cd implements CommandInterface {

  public CommandResult execute(List<String> args, ShellContext shell) {

    String target = "";
    String home = System.getenv("HOME");
    if (home == null || home.isBlank()) {
      home = System.getProperty("user.home");
    }

    if (args.isEmpty()) {
      target = home;
    } else if (args.size() == 1) {
      target = args.getFirst();
      if (target.startsWith("~")) {
        target = home + target.substring(1);
      }
    } else {
      shell.err().println("Too many arguments");
      CommandResult.continueWith(1);
    }

    Path path = shell.getCwd().resolve(target).normalize();

    if (Files.isDirectory(path)) {
      shell.setCwd(path);
      return CommandResult.continueWith(0);
    } else {
      shell.err().println("cd: " + path + ": No such file or directory");
      return CommandResult.continueWith(1);
    }
  }

}
