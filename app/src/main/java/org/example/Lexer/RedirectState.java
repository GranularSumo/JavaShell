package org.example.Lexer;

import org.example.Lexer.Token.TokenType;

public final class RedirectState implements LexerState {

  public static final RedirectState INSTANCE = new RedirectState();

  private RedirectState() {
  }

  @Override
  public LexerState processChar(char c, LexerContext context) {
    if (c == '>') {
      context.appendToToken(c);
      context.saveTokenIfNotEmpty(TokenType.OP);
      return UnquotedState.INSTANCE;
    }
    context.saveTokenIfNotEmpty(TokenType.OP);

    switch (c) {
      case ' ':
        return UnquotedState.INSTANCE;
      case '\'':
        return SingleQuotedState.INSTANCE;
      case '"':
        return DoubleQuotedState.INSTANCE;
      default:
        context.appendToToken(c);
        return UnquotedState.INSTANCE;
    }
  }

  @Override
  public void finalise(LexerContext context) {
    context.saveTokenIfNotEmpty(TokenType.OP);
  }

}
