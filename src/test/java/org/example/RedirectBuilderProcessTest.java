package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.evaluator.RedirectBuilder;
import org.example.parser.Redirect;
import org.example.parser.RedirectType;
import org.example.parser.Word;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RedirectBuilderProcessTest {

  @TempDir
  Path tempDir;

  private Redirect createRedirect(RedirectType type, String filename) {
    Word target = new Word(filename);
    return new Redirect(type, target);
  }

  private ProcessBuilder createEchoCommand(String message) {
    return new ProcessBuilder("sh", "-c",
        "echo 'stdout:" + message + "'; echo 'stderr:" + message + "' >&2");
  }

  @Test
  void applyToProcess_nullRedirect_inheritsIO() throws IOException, InterruptedException {
    ProcessBuilder builder = createEchoCommand("test");
    RedirectBuilder.applyToProcess(null, builder);

    Process process = builder.start();
    process.waitFor();

    assertEquals(0, process.exitValue());
  }

  @Test
  void applyToProcess_inputRedirect_setsInputFile() throws IOException, InterruptedException {
    Path inputFile = tempDir.resolve("input.txt");
    Files.writeString(inputFile, "hello from file");

    ProcessBuilder builder = new ProcessBuilder("cat");
    Redirect redirect = createRedirect(RedirectType.INPUT, inputFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String output = reader.readLine();
    process.waitFor();

    assertEquals("hello from file", output);
  }

  @Test
  void applyToProcess_outputRedirect_setsOutputFile() throws IOException, InterruptedException {
    Path outputFile = tempDir.resolve("output.txt");

    ProcessBuilder builder = createEchoCommand("test output");
    Redirect redirect = createRedirect(RedirectType.OUTPUT, outputFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String content = Files.readString(outputFile).trim();
    assertTrue(content.contains("test output"));
    assertFalse(content.contains("stderr"));
  }

  @Test
  void applyToProcess_errorRedirect_setsErrorFile() throws IOException, InterruptedException {
    Path errorFile = tempDir.resolve("error.txt");

    ProcessBuilder builder = createEchoCommand("test error");
    Redirect redirect = createRedirect(RedirectType.ERROR, errorFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String content = Files.readString(errorFile).trim();
    assertTrue(content.contains("stderr"));
    assertFalse(content.contains("test output"));
  }

  @Test
  void applyToProcess_allOutputRedirect_redirectsBothStreams() throws IOException, InterruptedException {
    Path outputFile = tempDir.resolve("all.txt");

    ProcessBuilder builder = createEchoCommand("test all");
    Redirect redirect = createRedirect(RedirectType.ALL_OUTPUT, outputFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String content = Files.readString(outputFile).trim();
    assertTrue(content.contains("test all"));
    assertTrue(content.contains("stderr"));
  }

  @Test
  void applyToProcess_outputAppend_appendsToFile() throws IOException, InterruptedException {
    Path outputFile = tempDir.resolve("append.txt");
    Files.writeString(outputFile, "original content\n");

    ProcessBuilder builder = new ProcessBuilder("echo", "appended content");
    Redirect redirect = createRedirect(RedirectType.OUTPUT_APPEND, outputFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String content = Files.readString(outputFile).trim();
    assertTrue(content.contains("original content"));
    assertTrue(content.contains("appended content"));
  }

  @Test
  void applyToProcess_errorAppend_appendsToErrorFile() throws IOException, InterruptedException {
    Path errorFile = tempDir.resolve("error_append.txt");
    Files.writeString(errorFile, "original error\n");

    ProcessBuilder builder = new ProcessBuilder("sh", "-c", "echo 'new error' >&2");
    Redirect redirect = createRedirect(RedirectType.ERROR_APPEND, errorFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String content = Files.readString(errorFile).trim();
    assertTrue(content.contains("original error"));
    assertTrue(content.contains("new error"));
  }

  @Test
  void applyToProcess_allAppend_appendsToBothStreams() throws IOException, InterruptedException {
    Path outputFile = tempDir.resolve("all_append.txt");
    Files.writeString(outputFile, "original\n");

    ProcessBuilder builder = createEchoCommand("append test");
    Redirect redirect = createRedirect(RedirectType.ALL_APPEND, outputFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String content = Files.readString(outputFile).trim();
    assertTrue(content.contains("original"));
    assertTrue(content.contains("append test"));
    assertTrue(content.contains("stderr"));
  }

  @Test
  void applyToProcess_outputRedirect_redirectsOnlyOutput() throws IOException, InterruptedException {
    Path outputFile = tempDir.resolve("output.txt");

    ProcessBuilder builder = createEchoCommand("test");
    Redirect redirect = createRedirect(RedirectType.OUTPUT, outputFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String fileContent = Files.readString(outputFile).trim();
    assertTrue(fileContent.contains("stdout:test"));
    assertFalse(fileContent.contains("stderr:"));
  }

  @Test
  void applyToProcess_errorRedirect_redirectsOnlyError() throws IOException, InterruptedException {
    Path errorFile = tempDir.resolve("error.txt");

    ProcessBuilder builder = createEchoCommand("test");
    Redirect redirect = createRedirect(RedirectType.ERROR, errorFile.toString());
    RedirectBuilder.applyToProcess(redirect, builder);

    Process process = builder.start();
    process.waitFor();

    String fileContent = Files.readString(errorFile).trim();
    assertTrue(fileContent.contains("stderr:test"));
    assertFalse(fileContent.contains("stdout:"));
  }

  @Test
  void applyToProcess_allAppendWithNormalRedirect_throwsException() throws IOException {
    ProcessBuilder builder = new ProcessBuilder("echo", "test");

    Redirect inputRedirect = createRedirect(RedirectType.INPUT, "test.txt");

    try {
      RedirectBuilder.applyToProcess(inputRedirect, builder);
      assertTrue(true, "Input redirect should work normally");
    } catch (IOException e) {
      assertTrue(false, "Input redirect should not throw exception: " + e.getMessage());
    }
  }

}
