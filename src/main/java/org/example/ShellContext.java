package org.example;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.example.evaluator.IoContext;

public class ShellContext {
  private final IoContext io;
  private CwdState cwdState = new CwdState(Path.of(System.getProperty("user.dir")));
  private boolean shouldContinue = true;

  // this allows any IoContext created using withIo() to share the ShellContext's
  // cwd.
  private static class CwdState {
    private Path cwd;

    public CwdState(Path cwd) {
      this.cwd = cwd;
    }

    public void setCwd(Path newPath) {
      this.cwd = newPath;
    }
  }

  private ShellContext(IoContext io, CwdState cwdState) {
    this.io = io;
    this.cwdState = cwdState;
  }

  /***
   * Creates a new {@code ShellContext} with the supplied {@link IoContext}
   * sharing the same current working directory state as this context.
   * 
   * @param io the {@link IoContext} to use.
   * @return a new context using {@code io} and the same working directory state.
   */

  public ShellContext withIo(IoContext io) {
    return new ShellContext(io, this.cwdState);
  }

  public ShellContext(IoContext io) {
    this.io = io;
  }

  public void setCwd(Path newAbsolutePath) {
    cwdState.setCwd(newAbsolutePath);
  }

  public Path getCwd() {
    return cwdState.cwd;
  }

  public PrintWriter out() {
    return io.out();
  }

  public PrintWriter err() {
    return io.err();
  }

  public BufferedReader in() {
    return io.in();
  }

  public IoContext stdio() {
    return io;
  }

  public boolean shouldContinue() {
    return shouldContinue;
  }

  public void toggleShouldContinue() {
    shouldContinue = !shouldContinue;
  }

}
