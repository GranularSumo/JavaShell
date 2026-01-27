package org.example.commands;

import java.util.List;

import org.example.Evaluator.CommandResult;
import org.example.ShellContext;

public interface CommandInterface {
  CommandResult execute(List<String> args, ShellContext context);
}
