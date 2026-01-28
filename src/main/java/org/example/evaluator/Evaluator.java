package org.example.evaluator;

import java.text.ParseException;

import org.example.ShellContext;
import org.example.commands.Builtins;
import org.example.parser.Command;
import org.example.parser.Parser;

public class Evaluator {
  private final ShellContext ctx;
  private final Builtins builtins;

  public Evaluator(ShellContext ctx, Builtins builtins) {
    this.ctx = ctx;
    this.builtins = builtins;
  }

  public CommandResult evaluate(String line, ShellContext ctx) {
    if (line.trim().isEmpty()) {
      return CommandResult.continueWith(0);
    }

    try {
      Command cmd = Parser.parse(line);

      if (builtins.isBuiltin(cmd.args().get(0).content())) {

      } else if (CommandUtils.getCommandFilepath(cmd.args().get(0).content()).isPresent()) {

      } else {
        ctx.err().println(cmd.args().get(0) + ": command not found");
        return CommandResult.continueWith(127);
      }

      return CommandResult.continueWith(0);
    } catch (ParseException e) {
      ctx.err().println(e.getMessage());
      ctx.err().flush();
      return CommandResult.continueWith(1);
    }
  }

public CommandResult executeBuiltin(Command cmd, Redirect)
