package org.example.evaluator;

public class CommandResult {
  private final int exitCode;
  private final boolean shouldExit;

  private CommandResult(int exitCode, boolean shouldExit) {
    this.exitCode = exitCode;
    this.shouldExit = shouldExit;
  }

  public static CommandResult continueWith(int exitCode) {
    return new CommandResult(exitCode, false);
  }

  public static CommandResult exitWith(int exitCode) {
    return new CommandResult(exitCode, true);
  }

  public int getExitCode() {
    return exitCode;
  }

  public boolean shouldExit() {
    return shouldExit;
  }
}