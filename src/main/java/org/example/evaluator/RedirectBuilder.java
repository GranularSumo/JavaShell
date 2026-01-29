package org.example.evaluator;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;

import org.example.evaluator.IoContext.Owns;
import org.example.parser.Redirect;

public class RedirectBuilder {

  public static IoContext applyAllToIoContext(List<Redirect> redirects, IoContext io) throws IOException {
    if (redirects.isEmpty()) {
      return io;
    }

    BufferedReader in = io.in();
    PrintWriter out = io.out();
    PrintWriter err = io.err();
    EnumSet<Owns> owned = EnumSet.noneOf(Owns.class);

    for (Redirect r : redirects) {
      switch (r.type()) {
        case INPUT -> {
          closeIfOwned(in, owned, Owns.IN);
          in = new BufferedReader(new FileReader(r.target().content()));
          owned.add(Owns.IN);
        }
        case OUTPUT, OUTPUT_APPEND -> {
          closeIfOwned(out, owned, Owns.OUT);
          out = createWriter(r);
          owned.add(Owns.OUT);
        }
        case ERROR, ERROR_APPEND -> {
          closeIfOwned(err, owned, Owns.ERR);
          err = createWriter(r);
          owned.add(Owns.ERR);
        }
        case ALL_OUTPUT, ALL_APPEND -> {
          closeIfOwned(out, owned, Owns.OUT);
          closeIfOwned(err, owned, Owns.ERR);
          PrintWriter pw = createWriter(r);
          out = err = pw;
          owned.add(Owns.OUT);
          owned.add(Owns.ERR);
        }
        default -> throw new IOException("Invalid RedirectType: " + r.type());
      }
    }
    return new IoContext(in, out, err, Owns.from(owned));
  }

  private static PrintWriter createWriter(Redirect r) throws IOException {
    boolean append = r.isAppendMode();
    return new PrintWriter(new FileWriter(r.target().content(), append), append);
  }

  private static void closeIfOwned(Closeable resource, EnumSet<Owns> owned, Owns owns) throws IOException {
    if (owned.contains(owns)) {
      resource.close();
    }
  }

  public static void applyAllToProcess(List<Redirect> redirects, ProcessBuilder builder, IoContext io)
      throws IOException {

    builder.inheritIO();

    for (Redirect r : redirects) {
      File f = checkAndPrepareFile(r);
      updateBuilderForRedirect(r, builder, f);
    }

  }

  private static void updateBuilderForRedirect(Redirect r, ProcessBuilder builder, File f) throws IOException {

    switch (r.type()) {
      case INPUT -> builder.redirectInput(f);
      case OUTPUT -> builder.redirectOutput(f);
      case OUTPUT_APPEND -> builder.redirectOutput(ProcessBuilder.Redirect.appendTo(f));
      case ERROR -> builder.redirectError(f);
      case ERROR_APPEND -> builder.redirectError(ProcessBuilder.Redirect.appendTo(f));
      case ALL_OUTPUT -> {
        builder.redirectOutput(f);
        builder.redirectError(f);
      }
      case ALL_APPEND -> {
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(f));
        builder.redirectError(ProcessBuilder.Redirect.appendTo(f));
      }
      default -> throw new IOException("Invalid Redirect");
    }
  }

  private static File checkAndPrepareFile(Redirect r) throws IOException {
    Path p = Path.of(r.target().content());
    switch (r.type()) {
      case INPUT: {
        return checkFileExistsAndReturn(p);
      }
      case OUTPUT, ERROR, ALL_OUTPUT: {
        return checkForWriteAndTruncate(p);
      }
      case OUTPUT_APPEND, ERROR_APPEND, ALL_APPEND: {
        return checkForWriteAndAppend(p);
      }
      default: {
        throw new IOException("Invalid Redirect");
      }
    }
  }

  private static File checkFileExistsAndReturn(Path p) throws IOException {
    if (Files.exists(p)) {
      return p.toFile();
    }
    throw new IOException("file: " + p.toString() + " does not have read access");
  }

  private static File checkForWriteAndTruncate(Path p) throws IOException {
    try {
      Files.write(p, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      return p.toFile();
    } catch (IOException e) {
      throw new IOException("file: " + p.toString() + " does not have write access");
    }
  }

  private static File checkForWriteAndAppend(Path p) throws IOException {
    try {
      Files.write(p, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      return p.toFile();
    } catch (IOException e) {
      throw new IOException("file: " + p.toString() + " does not have write access");
    }
  }

}
