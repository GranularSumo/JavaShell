package org.example.Lexer;

import java.util.List;

public class Lexer {
  private LexerContext context;
  private LexerState state = UnquotedState.INSTANCE;

  public Lexer(String input) {
    this.context = new LexerContext(input);
  }

  public static List<Token> tokenize(String input) {
    return new Lexer(input).tokenize();
  }

  private List<Token> tokenize() {
    String input = context.getInput();

    for (int i = 0; i < input.length(); i++) {
      state = state.processChar(input.charAt(i), context);
    }

    state.finalise(context);
    return context.getTokens();
  }
}
