package org.example;

import java.util.ArrayList;
import java.util.List;

import org.example.Lexer.Lexer;
import org.example.Lexer.Token;

public class Parser {
  private List<Token> tokenList;

  public Parser(String input) {
    tokenList = Lexer.tokenize(input);
  }

  public static void parse(String input) {
    Parser parser = new Parser(input);
    parser.parse();
  }

  private void parse() {
    System.out.println("do a thing");
  }

}
