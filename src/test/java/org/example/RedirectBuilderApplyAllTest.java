package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.example.evaluator.IoContext;
import org.example.evaluator.RedirectHandler;
import org.example.evaluator.IoContext.Owns;
import org.example.parser.Redirect;
import org.example.parser.RedirectType;
import org.example.parser.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Comprehensive tests for RedirectBuilder.applyAllToIoContext() method.
 * 
 * Tests multiple redirection scenarios including:
 * - Multiple output redirections (> file1.txt > file2.txt)
 * - Mixed output and error redirections (> file1.txt 2> file2.txt)
 * - Combined redirections (> file1.txt &> file2.txt)
 * - Input and output combinations (< input.txt > output.txt)
 * - Edge cases and resource management
 */
class RedirectBuilderApplyAllTest {

  @TempDir
  Path tempDir;

  private IoContext baseIoContext;

  @BeforeEach
  void setUp() {
    baseIoContext = new IoContext(
        new BufferedReader(new InputStreamReader(System.in)),
        new PrintWriter(System.out),
        new PrintWriter(System.err),
        Owns.NONE);
  }

  private Redirect createRedirect(RedirectType type, String filename) {
    Word target = new Word(filename);
    return new Redirect(type, target);
  }

  private List<Redirect> createRedirects(RedirectType... types) {
    List<Redirect> redirects = new ArrayList<>();
    for (int i = 0; i < types.length; i++) {
      String filename = "file" + (i + 1) + ".txt";
      redirects.add(createRedirect(types[i], tempDir.resolve(filename).toString()));
    }
    return redirects;
  }

  // Test Case 1: Multiple Output Redirections
  // Simulates: echo "test" > file1.txt > file2.txt
  @Test
  void testMultipleOutputRedirects_LastOneWins() throws IOException {
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.OUTPUT, file1.toString()),
        createRedirect(RedirectType.OUTPUT, file2.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    // Write output to test which file receives it
    result.out().println("test output");
    result.closeResources();

    // Only the last redirect (file2) should contain output
    assertEquals("test output", Files.readString(file2).trim());

    // First file should be created but empty (truncated when second redirect
    // applied)
    assertTrue(Files.exists(file1));
    assertEquals("", Files.readString(file1).trim());
  }

  // Test Case 2: Output and Error Separation
  // Simulates: echo "test" > file1.txt 2> file2.txt
  @Test
  void testOutputAndErrorRedirect_Separate() throws IOException {
    Path outputFile = tempDir.resolve("output.txt");
    Path errorFile = tempDir.resolve("error.txt");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.OUTPUT, outputFile.toString()),
        createRedirect(RedirectType.ERROR, errorFile.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    // Test that streams are properly separated
    result.out().println("stdout content");
    result.err().println("stderr content");
    result.closeResources();

    assertEquals("stdout content", Files.readString(outputFile).trim());
    assertEquals("stderr content", Files.readString(errorFile).trim());

    // Input should remain unchanged (same as base)
    assertSame(baseIoContext.in(), result.in());
  }

  // Test Case 3: Combined Output After Regular Output
  // Simulates: echo "test" > file1.txt &> file2.txt
  @Test
  void testOutputThenCombinedRedirect_CombinedWins() throws IOException {
    Path regularOutput = tempDir.resolve("regular.txt");
    Path combinedOutput = tempDir.resolve("combined.txt");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.OUTPUT, regularOutput.toString()),
        createRedirect(RedirectType.ALL_OUTPUT, combinedOutput.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    // Both stdout and stderr should go to combined file
    result.out().println("stdout to combined");
    result.err().println("stderr to combined");
    result.closeResources();

    // Combined file should have both outputs
    String combinedContent = Files.readString(combinedOutput).trim();
    assertTrue(combinedContent.contains("stdout to combined"));
    assertTrue(combinedContent.contains("stderr to combined"));

    // Regular output file should be empty (was truncated)
    assertTrue(Files.exists(regularOutput));
    assertEquals("", Files.readString(regularOutput).trim());
  }

  // Test Case 4: Input and Output Combination
  // Simulates: cat < input.txt > output.txt
  @Test
  void testInputAndOutputRedirect_BothApplied() throws IOException {
    Path inputFile = tempDir.resolve("input.txt");
    Path outputFile = tempDir.resolve("output.txt");

    // Setup input file with content
    Files.writeString(inputFile, "input content line 1\ninput content line 2");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.INPUT, inputFile.toString()),
        createRedirect(RedirectType.OUTPUT, outputFile.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    // Verify input redirection
    assertEquals("input content line 1", result.in().readLine());
    assertEquals("input content line 2", result.in().readLine());

    // Verify output redirection
    result.out().println("processed output");
    result.closeResources();

    assertEquals("processed output", Files.readString(outputFile).trim());

    // Error stream should remain unchanged
    assertSame(baseIoContext.err(), result.err());
  }

  // Test Case 5: Multiple Input Redirections
  // Simulates: command < file1.txt < file2.txt
  @Test
  void testMultipleInputRedirects_LastOneWins() throws IOException {
    Path input1 = tempDir.resolve("input1.txt");
    Path input2 = tempDir.resolve("input2.txt");

    Files.writeString(input1, "content from file 1");
    Files.writeString(input2, "content from file 2");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.INPUT, input1.toString()),
        createRedirect(RedirectType.INPUT, input2.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    // Should read from the last input file (file2)
    assertEquals("content from file 2", result.in().readLine());
    result.closeResources();
  }

  // Test Case 6: Complex Redirection Chain
  // Simulates: command < input.txt > output1.txt > output2.txt 2> error.txt
  @Test
  void testComplexRedirectionChain() throws IOException {
    Path inputFile = tempDir.resolve("input.txt");
    Path output1 = tempDir.resolve("output1.txt");
    Path output2 = tempDir.resolve("output2.txt");
    Path errorFile = tempDir.resolve("error.txt");

    Files.writeString(inputFile, "input data");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.INPUT, inputFile.toString()),
        createRedirect(RedirectType.OUTPUT, output1.toString()),
        createRedirect(RedirectType.OUTPUT, output2.toString()),
        createRedirect(RedirectType.ERROR, errorFile.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    // Test all streams
    assertEquals("input data", result.in().readLine());
    result.out().println("final output");
    result.err().println("error message");
    result.closeResources();

    // Verify final destinations
    assertEquals("final output", Files.readString(output2).trim()); // Last output wins
    assertEquals("error message", Files.readString(errorFile).trim());

    // First output file should be truncated
    assertTrue(Files.exists(output1));
    assertEquals("", Files.readString(output1).trim());
  }

  // Test Case 7: Mixed Append and Overwrite
  // Simulates: command > file.txt >> file.txt
  @Test
  void testMixedAppendAndOverwrite() throws IOException {
    Path targetFile = tempDir.resolve("target.txt");

    // Pre-populate file
    Files.writeString(targetFile, "existing content\n");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.OUTPUT, targetFile.toString()), // Overwrite
        createRedirect(RedirectType.OUTPUT_APPEND, targetFile.toString()) // Append
    );

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    result.out().println("appended content");
    result.closeResources();

    // The file should be truncated by first redirect, then appended to
    String content = Files.readString(targetFile).trim();
    assertEquals("appended content", content);
  }

  // Test Case 8: Empty Redirect List
  @Test
  void testEmptyRedirectList() throws IOException {
    List<Redirect> emptyRedirects = List.of();

    IoContext result = RedirectHandler.applyAllToIoContext(emptyRedirects, baseIoContext);

    // Should return the original context unchanged
    assertSame(baseIoContext, result);
  }

  // Test Case 9: Single Redirect Works Correctly
  @Test
  void testSingleRedirect_BehavesLikeApplyToIoContext() throws IOException {
    Path outputFile = tempDir.resolve("single.txt");

    List<Redirect> singleRedirect = List.of(
        createRedirect(RedirectType.OUTPUT, outputFile.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(singleRedirect, baseIoContext);

    // Should create new context (not same as base)
    assertNotSame(baseIoContext, result);

    // Test that it works correctly
    result.out().println("single redirect test");
    result.closeResources();

    assertEquals("single redirect test", Files.readString(outputFile).trim());

    // Input and error should remain unchanged from base context
    assertSame(baseIoContext.in(), result.in());
    assertSame(baseIoContext.err(), result.err());
  }

  // Test Case 10: Multiple Error Redirections
  // Simulates: command 2> file1.txt 2> file2.txt
  @Test
  void testMultipleErrorRedirects_LastOneWins() throws IOException {
    Path error1 = tempDir.resolve("error1.txt");
    Path error2 = tempDir.resolve("error2.txt");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.ERROR, error1.toString()),
        createRedirect(RedirectType.ERROR, error2.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    result.err().println("error message");
    result.closeResources();

    // Only the last error redirect should contain output
    assertEquals("error message", Files.readString(error2).trim());

    // First error file should be truncated
    assertTrue(Files.exists(error1));
    assertEquals("", Files.readString(error1).trim());
  }

  // Test Case 11: Combined Output Redirections
  // Simulates: command &> file1.txt &> file2.txt
  @Test
  void testMultipleCombinedRedirects_LastOneWins() throws IOException {
    Path combined1 = tempDir.resolve("combined1.txt");
    Path combined2 = tempDir.resolve("combined2.txt");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.ALL_OUTPUT, combined1.toString()),
        createRedirect(RedirectType.ALL_OUTPUT, combined2.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    result.out().println("stdout message");
    result.err().println("stderr message");
    result.closeResources();

    // Only the last combined redirect should receive output
    String combined2Content = Files.readString(combined2).trim();
    assertTrue(combined2Content.contains("stdout message"));
    assertTrue(combined2Content.contains("stderr message"));

    // First combined file should be truncated
    assertTrue(Files.exists(combined1));
    assertEquals("", Files.readString(combined1).trim());
  }

  // Test Case 12: Append Mode Precedence
  // Simulates: command >> file1.txt > file2.txt
  @Test
  void testAppendThenOverwrite() throws IOException {
    Path appendFile = tempDir.resolve("append.txt");
    Path overwriteFile = tempDir.resolve("overwrite.txt");

    // Pre-populate append file
    Files.writeString(appendFile, "existing content\n");

    List<Redirect> redirects = List.of(
        createRedirect(RedirectType.OUTPUT_APPEND, appendFile.toString()),
        createRedirect(RedirectType.OUTPUT, overwriteFile.toString()));

    IoContext result = RedirectHandler.applyAllToIoContext(redirects, baseIoContext);

    result.out().println("new content");
    result.closeResources();

    // Output should go to overwrite file (last redirect wins)
    assertEquals("new content", Files.readString(overwriteFile).trim());

    // Append file should remain unchanged (wasn't used)
    assertEquals("existing content", Files.readString(appendFile).trim());
  }
}
