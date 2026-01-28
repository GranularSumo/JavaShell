package org.example;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.lexer.Token.TokenType;
import org.junit.jupiter.api.Test;

class LexerTest {

  @Test
  public void tokenizesSimpleInput() {
    List<Token> tokens = Lexer.tokenize("Hello World.");
    assertFalse(tokens.isEmpty());

    List<Token> expectedTokens = new ArrayList<>();
    expectedTokens.add(new Token(TokenType.WORD, "Hello"));
    expectedTokens.add(new Token(TokenType.WORD, "World."));
    assertEquals(expectedTokens, tokens);
  }

  @Test
  public void correctlyTokenizesRedirects() {
    List<Token> tokens = Lexer.tokenize("This is a redirect 1> target.filetype");
    assertEquals(6, tokens.size());
    assertEquals(TokenType.REDIRECT, tokens.get(4).type());
  }

  @Test
  public void correctlyHandlesRedirectCases() {
    assertTokens(
        "cmd > file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, ">",
        TokenType.WORD, "file");

    assertTokens("cmd < file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "<",
        TokenType.WORD, "file");

    assertTokens("cmd >> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, ">>",
        TokenType.WORD, "file");

    assertTokens("cmd 1> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "1>",
        TokenType.WORD, "file");

    assertTokens("cmd 2> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "2>",
        TokenType.WORD, "file");

    assertTokens("cmd 1>> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "1>>",
        TokenType.WORD, "file");

    assertTokens("cmd 2>> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "2>>",
        TokenType.WORD, "file");

    assertTokens("cmd &> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "&>",
        TokenType.WORD, "file");

    assertTokens("cmd &>> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "&>>",
        TokenType.WORD, "file");

    assertTokens("cmd 1>>> file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, "1>>",
        TokenType.REDIRECT, ">",
        TokenType.WORD, "file");

    assertTokens("cmd>file",
        TokenType.WORD, "cmd",
        TokenType.REDIRECT, ">",
        TokenType.WORD, "file");

    assertTokens("cmd1>file",
        TokenType.WORD, "cmd1",
        TokenType.REDIRECT, ">",
        TokenType.WORD, "file");

    assertTokens("2>file",
        TokenType.REDIRECT, "2>",
        TokenType.WORD, "file");

    assertTokens("cmd 3> file",
        TokenType.WORD, "cmd",
        TokenType.WORD, "3",
        TokenType.REDIRECT, ">",
        TokenType.WORD, "file");

    assertTokens("cmd abc> file",
        TokenType.WORD, "cmd",
        TokenType.WORD, "abc",
        TokenType.REDIRECT, ">",
        TokenType.WORD, "file");
  }

  private void assertTokens(String input, Object... expectedTokens) {
    List<Token> tokens = Lexer.tokenize(input);
    assertEquals(expectedTokens.length / 2, tokens.size());

    for (int i = 0; i < tokens.size(); i++) {
      assertEquals(expectedTokens[i * 2], tokens.get(i).type());
      assertEquals(expectedTokens[i * 2 + 1], tokens.get(i).value());
    }
  }
}
