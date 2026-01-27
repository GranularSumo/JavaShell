package org.example.Evaluator;

import org.example.ShellContext;
import org.example.commands.Builtins;

public class Evaluator {
  private final ShellContext ctx;
  private final Builtins builtins;

  public Evaluator(ShellContext ctx, Builtins builtins) {
    this.ctx = ctx;
    this.builtins = builtins;
  }

  public static void evaluate(String line, ShellContext ctx) {

  }

}
