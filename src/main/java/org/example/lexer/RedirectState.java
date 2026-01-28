package org.example.lexer;

import org.example.lexer.Token.TokenType;
import org.example.parser.RedirectType;

public final class RedirectState implements LexerState {

  public static final RedirectState INSTANCE = new RedirectState();

  private RedirectState() {
  }

  @Override
  public LexerState processChar(char c, LexerContext context) {
    String potentialRedirect = context.getCurrentTokenContent() + c;

    if (RedirectType.isValidRedirectType(potentialRedirect)) {
      context.appendToToken(c);
      return this;
    } else {
      context.saveTokenIfNotEmpty(TokenType.REDIRECT);
    }

    switch (c) {
      case ' ':
        return UnquotedState.INSTANCE;
      case '\'':
        return SingleQuotedState.INSTANCE;
      case '"':
        return DoubleQuotedState.INSTANCE;
      case '<':
        context.appendToToken(c);
        context.saveTokenIfNotEmpty(TokenType.REDIRECT);
        return UnquotedState.INSTANCE;
      case '>':
        context.appendToToken(c);
        return this;
      default:
        context.appendToToken(c);
        return UnquotedState.INSTANCE;
    }
  }

  @Override
  public void finalise(LexerContext context) {
    context.saveTokenIfNotEmpty(TokenType.REDIRECT);
  }

}
