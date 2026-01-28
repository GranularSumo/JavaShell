package org.example.commands;

import java.util.List;

import org.example.evaluator.CommandResult;
import org.example.ShellContext;

public interface CommandInterface {
  CommandResult execute(List<String> args, ShellContext context);
}
