package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.evaluator.IoContext;
import org.example.evaluator.RedirectBuilder;
import org.example.evaluator.IoContext.Owns;
import org.example.parser.Redirect;
import org.example.parser.RedirectType;
import org.example.parser.Word;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RedirectBuilderIoContextTest {

  @TempDir
  Path tempDir;

  IoContext ctx = new IoContext(new BufferedReader(new InputStreamReader(System.in)), new PrintWriter(System.out),
      new PrintWriter(System.err),
      Owns.NONE);

  private Redirect createRedirect(RedirectType type, String filename) {
    Word target = new Word(filename);
    return new Redirect(type, target);
  }

  @Test
  void applyToIoContext_nullRedirect_returnsSameIoContext() throws IOException {
    IoContext result = RedirectBuilder.applyToIoContext(null, ctx);
    assertSame(ctx, result);
  }

  @Test
  void applyToIoContext_inputRedirect_createsReaderFromFile() throws IOException {
    Path inputFile = tempDir.resolve("input.txt");
    Files.writeString(inputFile, "hello world");

    Redirect redirect = createRedirect(RedirectType.INPUT, inputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    assertNotSame(ctx, result);
    assertEquals("hello world", result.in().readLine());
    result.in().close();
  }

  @Test
  void applyToIoContext_outputRedirect_createsWriterToFile() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Redirect redirect = createRedirect(RedirectType.OUTPUT, outputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    result.out().println("test output");
    result.out().close();

    assertEquals("test output", Files.readString(outputFile).trim());
  }

  @Test
  void applyToIoContext_errorRedirect_createsWriterToFile() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Redirect redirect = createRedirect(RedirectType.ERROR, outputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    result.err().println("test error");
    result.err().close();

    assertEquals("test error", Files.readString(outputFile).trim());
  }

  @Test
  void applyToIoContext_bothRedirect_createsWriterToFile() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Redirect redirect = createRedirect(RedirectType.ALL_OUTPUT, outputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    result.out().println("test output");
    result.err().println("test error");
    result.out().close();
    result.err().close();

    assertEquals("test output\ntest error", Files.readString(outputFile).trim());
  }

  @Test
  void applyToIoContext_outputRedirectAppend_createsWriterToFile() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Redirect redirect = createRedirect(RedirectType.OUTPUT, outputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    result.out().println("test output");
    result.err().println("test error");
    result.out().close();
    result.err().close();

    assertEquals("test output", Files.readString(outputFile).trim());

    Redirect redirect2 = createRedirect(RedirectType.OUTPUT_APPEND, outputFile.toString());
    result = RedirectBuilder.applyToIoContext(redirect2, ctx);

    result.out().println("test append output");
    result.err().println("test append error");
    result.out().close();
    result.err().close();

    assertEquals("test output\ntest append output",
        Files.readString(outputFile).trim());
  }

  @Test
  void applyToIoContext_errorRedirectAppend_createsWriterToFile() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Redirect redirect = createRedirect(RedirectType.ERROR, outputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    result.out().println("test output");
    result.err().println("test error");
    result.out().close();
    result.err().close();

    assertEquals("test error", Files.readString(outputFile).trim());

    Redirect redirect2 = createRedirect(RedirectType.ERROR_APPEND, outputFile.toString());
    result = RedirectBuilder.applyToIoContext(redirect2, ctx);

    result.out().println("test append output");
    result.err().println("test append error");
    result.out().close();
    result.err().close();

    assertEquals("test error\ntest append error",
        Files.readString(outputFile).trim());
  }

  @Test
  void applyToIoContext_bothRedirectAppend_createsWriterToFile() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Redirect redirect = createRedirect(RedirectType.ALL_OUTPUT, outputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    result.out().println("test output");
    result.err().println("test error");
    result.out().close();
    result.err().close();

    assertEquals("test output\ntest error", Files.readString(outputFile).trim());

    Redirect redirect2 = createRedirect(RedirectType.ALL_APPEND, outputFile.toString());
    result = RedirectBuilder.applyToIoContext(redirect2, ctx);

    result.out().println("test append output");
    result.err().println("test append error");
    result.out().close();
    result.err().close();

    assertEquals("test output\ntest error\ntest append output\ntest append error",
        Files.readString(outputFile).trim());
  }

  @Test
  void applyToIoContext_outputRedirect_nonRedirectedErrGoesToSystemErr() throws IOException {
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errContent));

    try {
      IoContext freshCtx = new IoContext(new BufferedReader(new InputStreamReader(System.in)),
          new PrintWriter(System.out), new PrintWriter(System.err), Owns.NONE);

      Path outputFile = tempDir.resolve("output.txt");
      Redirect redirect = createRedirect(RedirectType.OUTPUT, outputFile.toString());
      IoContext result = RedirectBuilder.applyToIoContext(redirect, freshCtx);

      result.out().println("test output to file");
      result.err().println("test error to system");
      result.out().close();
      result.err().close();

      assertEquals("test output to file", Files.readString(outputFile).trim());
      assertEquals("test error to system", errContent.toString().trim());
    } finally {
      System.setErr(originalErr);
    }
  }

  @Test
  void applyToIoContext_errorRedirect_nonRedirectedOutGoesToSystemOut() throws IOException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    try {
      IoContext freshCtx = new IoContext(new BufferedReader(new InputStreamReader(System.in)),
          new PrintWriter(System.out), new PrintWriter(System.err), Owns.NONE);

      Path outputFile = tempDir.resolve("output.txt");
      Redirect redirect = createRedirect(RedirectType.ERROR, outputFile.toString());
      IoContext result = RedirectBuilder.applyToIoContext(redirect, freshCtx);

      result.out().println("test output to system");
      result.err().println("test error to file");
      result.out().close();
      result.err().close();

      assertEquals("test error to file", Files.readString(outputFile).trim());
      assertEquals("test output to system", outContent.toString().trim());
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  void applyToIoContext_inputRedirect_nonRedirectedOutputsGoToSystem() throws IOException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    try {
      IoContext freshCtx = new IoContext(new BufferedReader(new InputStreamReader(System.in)),
          new PrintWriter(System.out), new PrintWriter(System.err), Owns.NONE);

      Path inputFile = tempDir.resolve("input.txt");
      Files.writeString(inputFile, "test input content");

      Redirect redirect = createRedirect(RedirectType.INPUT, inputFile.toString());
      IoContext result = RedirectBuilder.applyToIoContext(redirect, freshCtx);

      result.out().println("test output to system");
      result.err().println("test error to system");
      result.in().close();
      result.out().close();
      result.err().close();

      assertEquals("test output to system", outContent.toString().trim());
      assertEquals("test error to system", errContent.toString().trim());
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }
  }

  @Test
  void applyToIoContext_bothRedirect_nonRedirectedInGoesToSystemIn() throws IOException {
    Path inputFile = tempDir.resolve("input.txt");
    Files.writeString(inputFile, "hello from file");

    Redirect redirect = createRedirect(RedirectType.ALL_OUTPUT, inputFile.toString());
    IoContext result = RedirectBuilder.applyToIoContext(redirect, ctx);

    assertSame(ctx.in(), result.in());

    result.out().println("test output");
    result.err().println("test error");
    result.out().close();
    result.err().close();

    assertEquals("test output\ntest error", Files.readString(inputFile).trim());
  }
}
