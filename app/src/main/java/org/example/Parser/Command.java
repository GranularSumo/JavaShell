package org.example.Parser;

import java.util.List;

public record Command(List<Word> args, List<Redirect> redirects) {
  public Command {
    args = List.copyOf(args != null ? args : List.of());
    redirects = List.copyOf(redirects != null ? redirects : List.of());

    if (args.isEmpty()) {
      throw new IllegalArgumentException("Command must have at least one argument (the name of the command to run)");
    }
  }

  public Word commandName() {
    return args.get(0);
  }

  public List<Word> arguments() {
    return args.subList(1, args.size());
  }
}
