package org.example.lexer;

import java.util.ArrayList;
import java.util.List;

import org.example.lexer.Token.TokenType;

public class LexerContext {
  private String input;
  private List<Token> tokens = new ArrayList<>();
  private StringBuilder tokenBuilder = new StringBuilder();
  private boolean nextCharEscaped = false;

  public LexerContext(String input) {
    this.input = input;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokens(List<Token> tokens) {
    this.tokens = tokens;
  }

  public StringBuilder getTokenBuilder() {
    return tokenBuilder;
  }

  public void setTokenBuilder(StringBuilder tokenBuilder) {
    this.tokenBuilder = tokenBuilder;
  }

  public boolean isNextCharEscaped() {
    return nextCharEscaped;
  }

  public void setNextCharEscaped(boolean charIsEscaped) {
    this.nextCharEscaped = charIsEscaped;
  }

  public void appendToToken(char c) {
    tokenBuilder.append(c);
  }

  public String getCurrentTokenContent() {
    return tokenBuilder.toString();
  }

  public void saveTokenIfNotEmpty(TokenType type) {
    if (tokenBuilder.isEmpty()) {
      return;
    }
    tokens.add(new Token(type, tokenBuilder.toString()));
    tokenBuilder.setLength(0);
  }
}
