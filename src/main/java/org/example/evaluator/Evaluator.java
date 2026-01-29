package org.example.evaluator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.example.ShellContext;
import org.example.commands.Builtins;
import org.example.parser.Command;
import org.example.parser.Parser;
import org.example.parser.Word;

import com.google.common.collect.Lists;

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

      if (builtins.isBuiltin(cmd.args().get(0).content())) {

        String cmdName = cmd.args().get(0).content();
        List<String> args = new ArrayList<>();
        for (Word w : cmd.args().subList(1, cmd.args().size())) {
          args.add(w.content());
        }

        builtins.get(cmd.args().get(0).content()).execute(args, ctx.withIo(
            RedirectBuilder.applyAllToIoContext(cmd.redirects(), ctx.stdio())));

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
}
