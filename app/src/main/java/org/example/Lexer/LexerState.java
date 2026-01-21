package org.example.Lexer;

public interface LexerState {
  LexerState processChar(char c, LexerContext context);

  void finalise(LexerContext context);
}
