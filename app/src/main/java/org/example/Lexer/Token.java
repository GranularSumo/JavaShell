package org.example.Lexer;

public record Token(TokenType type, String value) {
  public enum TokenType {
    WORD,
    OP,
  }
}
