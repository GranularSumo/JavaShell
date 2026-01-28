package org.example.lexer;

import org.example.lexer.Token.TokenType;

public class DoubleQuotedState implements LexerState {

  public static final DoubleQuotedState INSTANCE = new DoubleQuotedState();

  private DoubleQuotedState() {
  }

  @Override
  public final LexerState processChar(char c, LexerContext context) {
    if (context.isNextCharEscaped() && "\"\\$`\n".indexOf(c) >= 0) {
      context.appendToToken(c);
      context.setNextCharEscaped(false);
      return this;
    } else if (context.isNextCharEscaped()) {
      context.appendToToken('\\');
      context.appendToToken(c);
      context.setNextCharEscaped(false);
      return this;
    }

    switch (c) {
      case '"':
        return UnquotedState.INSTANCE;
      case '\\':
        context.setNextCharEscaped(true);
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
}
