package org.example.lexer;

import org.example.lexer.Token.TokenType;

public final class SingleQuotedState implements LexerState {

  public static final SingleQuotedState INSTANCE = new SingleQuotedState();

  private SingleQuotedState() {
  }

  @Override
  public LexerState processChar(char c, LexerContext context) {
    if (c == '\'') {
      return UnquotedState.INSTANCE;
    }
    context.appendToToken(c);
    return this;
  }

  @Override
  public void finalise(LexerContext context) {
    context.saveTokenIfNotEmpty(TokenType.WORD);
  }
}
