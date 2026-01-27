package org.example.Lexer;

import org.example.Parser.Word;

public record Token(TokenType type, String value) {
  public enum TokenType {
    WORD,
    REDIRECT,
  }

  public Word convertToWord() {
    if (this.type == TokenType.REDIRECT) {
      throw new IllegalArgumentException("TokenType.REDIRECT cannot be converted into type: Word");
    }
    return new Word(this.value());
  }
}