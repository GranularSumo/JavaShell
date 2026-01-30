package org.example.evaluator;

import java.io.File;
import java.io.IOException;
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

  public CommandResult evaluate(String line, ShellContext ctx) throws IOException {
    if (line.trim().isEmpty()) {
      return CommandResult.continueWith(0);
    }

    try {
      Command cmd = Parser.parse(line);

      if (builtins.isBuiltin(cmd.commandNameAsString())) {
        executeBuiltin(cmd);
      } else if (CommandUtils.getCommandFilepath(cmd.commandNameAsString()).isPresent()) {
        executeExternal(cmd);
      } else {
        ctx.err().println(cmd.commandNameAsString() + ": command not found");
        return CommandResult.continueWith(127);
      }

      return CommandResult.continueWith(0);
    } catch (ParseException e) {
      ctx.err().println(e.getMessage());
      ctx.err().flush();
      return CommandResult.continueWith(1);
    }
  }

  public CommandResult executeBuiltin(Command cmd) throws IOException {
    IoContext context = RedirectHandler.applyAllToIoContext(cmd.redirects(), ctx.stdio());
    try {
      return builtins.get(cmd.commandNameAsString()).execute(cmd.argsAsStrings(), ctx.withIo(context));
    } finally {
      context.closeResources();
    }
  }

  public CommandResult executeExternal(Command cmd) throws IOException {
    try {
      ProcessBuilder builder = new ProcessBuilder(cmd.fullCommandAsList()).directory(new File(ctx.getCwd().toString()));
      RedirectHandler.applyAllToProcess(cmd.redirects(), builder, ctx.stdio());
      return CommandResult.continueWith(builder.start().waitFor());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      ctx.err().println(cmd.commandNameAsString() + ": interrupted");
      return CommandResult.continueWith(130);
    }
  }
}
