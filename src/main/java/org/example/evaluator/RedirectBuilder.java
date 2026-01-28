package org.example.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.example.evaluator.IoContext.Owns;
import org.example.parser.Redirect;
import org.example.parser.RedirectType;

public class RedirectBuilder {

  public static IoContext applyToIoContext(Redirect redirect, IoContext io) throws IOException {
    if (redirect == null) {
      return io;
    }

    if (redirect.type() == RedirectType.INPUT) {
      BufferedReader reader = new BufferedReader(new FileReader(redirect.target().content()));
      return new IoContext(reader, io.out(), io.err(), Owns.IN);
    }

    if (redirect.isAppendMode()) {
      PrintWriter pw = new PrintWriter(new FileWriter(redirect.target().content(), true), true);
      switch (redirect.type()) {
        case OUTPUT_APPEND:
          return new IoContext(io.in(), pw, io.err(), Owns.OUT);
        case ERROR_APPEND:
          return new IoContext(io.in(), io.out(), pw, Owns.ERR);
        case ALL_APPEND:
          return new IoContext(io.in(), pw, pw, Owns.ALL);
        default:
          throw new IOException("Invalid RedirectType: " + redirect.type());
      }
    } else {
      PrintWriter pw = new PrintWriter(new FileWriter(redirect.target().content()));
      switch (redirect.type()) {
        case OUTPUT:
          return new IoContext(io.in(), pw, io.err(), Owns.OUT);
        case ERROR:
          return new IoContext(io.in(), io.out(), pw, Owns.ERR);
        case ALL_OUTPUT:
          return new IoContext(io.in(), pw, pw, Owns.ALL);
        default:
          throw new IOException("Invalid RedirectType: " + redirect.type());
      }
    }
  }

  public static void applyToProcess(Redirect redirect, ProcessBuilder builder) throws IOException {
    if (redirect == null) {
      builder.inheritIO();
      return;
    }

    File f = new File(redirect.target().content());

    if (redirect.type() == RedirectType.INPUT) {
      builder.redirectInput(f);
      return;
    }

    if (redirect.isAppendMode()) {
      switch (redirect.type()) {
        case OUTPUT_APPEND:
          builder.redirectOutput(java.lang.ProcessBuilder.Redirect.appendTo(f))
              .redirectError(java.lang.ProcessBuilder.Redirect.INHERIT);
          break;
        case ERROR_APPEND:
          builder.redirectOutput(java.lang.ProcessBuilder.Redirect.INHERIT)
              .redirectError(java.lang.ProcessBuilder.Redirect.appendTo(f));
          break;
        case ALL_APPEND:
          builder.redirectOutput(java.lang.ProcessBuilder.Redirect.appendTo(f))
              .redirectError(java.lang.ProcessBuilder.Redirect.appendTo(f));
          break;
        default:
          throw new IOException("Invalid RedirectType: " + redirect.type());
      }
    } else {
      switch (redirect.type()) {
        case OUTPUT:
          builder.redirectOutput(f)
              .redirectError(java.lang.ProcessBuilder.Redirect.INHERIT);
          break;
        case ERROR:
          builder.redirectOutput(java.lang.ProcessBuilder.Redirect.INHERIT)
              .redirectError(f);
          break;
        case ALL_OUTPUT:
          builder.redirectOutput(f)
              .redirectError(f);
          break;
        default:
      }
    }
  }
}
