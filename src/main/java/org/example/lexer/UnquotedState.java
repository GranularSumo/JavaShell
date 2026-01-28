package org.example.lexer;

import org.example.lexer.Token.TokenType;
import org.example.parser.RedirectType;

public final class UnquotedState implements LexerState {

  public static final UnquotedState INSTANCE = new UnquotedState();

  private UnquotedState() {
  }

  @Override
  public LexerState processChar(char c, LexerContext context) {
    if (context.isNextCharEscaped()) {
      context.appendToToken(c);
      context.setNextCharEscaped(false);
      return this;
    }
    switch (c) {
      case '\'':
        return SingleQuotedState.INSTANCE;
      case '"':
        return DoubleQuotedState.INSTANCE;
      case '>':
        return checkRedirectPrefix(c, context);
      case '<':
        context.saveTokenIfNotEmpty(TokenType.WORD);
        context.appendToToken(c);
        context.saveTokenIfNotEmpty(TokenType.REDIRECT);
        return this;
      case '\\':
        context.setNextCharEscaped(true);
        return this;
      case ' ':
        context.saveTokenIfNotEmpty(TokenType.WORD);
        return this;
      default:
        context.appendToToken(c);
        return this;
    }
  }

  @Override
  public void finalise(LexerContext context) {
    context.saveTokenIfNotEmpty(TokenType.WORD);
  }

  private LexerState checkRedirectPrefix(char c, LexerContext context) {
    String currentToken = context.getCurrentTokenContent();

    boolean isValidRedirectPrefix = currentToken.isEmpty()
        || (currentToken.length() == 1 && RedirectType.isRedirectPrefix(currentToken.charAt(0)));

    if (!isValidRedirectPrefix) {
      context.saveTokenIfNotEmpty(TokenType.WORD);
    }

    context.appendToToken(c);
    return RedirectState.INSTANCE;
  }

}
