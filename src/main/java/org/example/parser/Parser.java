package org.example.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.example.lexer.Token;
import org.example.lexer.Lexer;

public class Parser {
  private List<Token> tokenList;
  private int currentIndex = 0;

  public Parser(String input) {
    tokenList = Lexer.tokenize(input.trim());
  }

  public static Command parse(String input) throws ParseException {
    Parser parser = new Parser(input);
    return parser.parseCommand();
  }

  private Command parseCommand() throws ParseException {
    List<Word> args = new ArrayList<>();
    List<Redirect> redirects = new ArrayList<>();

    while (currentIndex < tokenList.size()) {
      Token t = tokenList.get(currentIndex);

      if (t.type() == Token.TokenType.WORD) {
        args.add(t.convertToWord());
        currentIndex++;
      } else if (t.type() == Token.TokenType.REDIRECT) {
        Redirect redirect = parseRedirect();
        redirects.add(redirect);
      } else {
        throw new ParseException("Unexpected token: " + t, currentIndex);
      }
    }

    if (args.isEmpty()) {
      throw new ParseException("Command cannot be empty", currentIndex);
    }

    return new Command(args, redirects);
  }

  private Redirect parseRedirect() throws ParseException {
    Token t = tokenList.get(currentIndex);
    currentIndex++;

    RedirectType type = RedirectType.fromString(t.value());
    if (type == null) {
      throw new ParseException("Invalid redirect: " + t.value(), currentIndex);
    }

    if (currentIndex >= tokenList.size()) {
      throw new ParseException("Redirect operator requires a target file", currentIndex);
    }

    Token targetToken = tokenList.get(currentIndex);

    if (targetToken.type() != Token.TokenType.WORD) {
      throw new ParseException("Redirect target must be a word, got: TokenType." + targetToken.type(), currentIndex);
    }

    Word target = new Word(targetToken.value());
    currentIndex++;

    return new Redirect(type, target);

  }

}

/*
 * TODO: build an AST from the tokenList.
 * 
 *
 * Spec:
 *
 * Nodes:
 *
 * Word: String content
 * Redirect: RedirectType type, Word target
 * Command: List<Word> args, List<Redirect> redirects
 * Pipeline: List<Command> commands (will implement this at a later date.)
 *
 * can use this to properly truncate files on multiple redirects I think:
 * https://stackoverflow.com/a/36316178
 *
 * need to look further into how pipelines connect the outputs of commands.
 * I believe that the AST will need to be set up so that redirected outputs beat
 * pipelines.
 */
