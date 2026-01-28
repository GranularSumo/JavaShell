package org.example.lexer;

public interface LexerState {
  LexerState processChar(char c, LexerContext context);

  void finalise(LexerContext context);
}
