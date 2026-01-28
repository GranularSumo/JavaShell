package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;

import org.example.evaluator.IoContext;
import org.example.evaluator.IoContext.Owns;
import org.example.parser.Parser;

public class Shell {

  private ShellContext ctx;

  public Shell(ShellContext ctx) {
    this.ctx = ctx;
  }

  public static void main(String[] args) {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    PrintWriter out = new PrintWriter(System.out);
    PrintWriter err = new PrintWriter(System.err);
    IoContext stdIO = new IoContext(in, out, err, Owns.NONE);
    Shell shell = new Shell(new ShellContext(stdIO));
    shell.run();
  }

  private void run() {
    try {
      while (ctx.shouldContinue()) {
        prompt();
        String input = readInput();

        if (input == null) {
          ctx.shouldContinue();
          break;
        }

        if (input.isBlank()) {
          continue;
        }

        try {
          ctx.out().println(Parser.parse(input));
        } catch (ParseException e) {
          ctx.err().println(e.getMessage());
          ctx.err().flush();
        }
      }
    } catch (IOException e) {
      ctx.err().println("I/O error: " + e.getMessage());
    }
  }

  private void prompt() {
    ctx.out().print("\u001B[1;34m");
    ctx.out().print(ctx.getCwd().toString() + " >> ");
    ctx.out().print("\u001B[0m");
    ctx.out().flush();
  }

  private String readInput() throws IOException {
    StringBuilder input = new StringBuilder();
    while (true) {
      String line = ctx.in().readLine();

      if (line == null) {
        return null;
      }

      if (line.endsWith("\\")) {
        input.append(line, 0, line.length() - 1);
      } else {
        input.append(line);
        break;
      }
    }
    return input.toString();
  }
}
