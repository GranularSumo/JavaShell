package org.example.evaluator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;

public record IoContext(BufferedReader in, PrintWriter out, PrintWriter err, Owns owns) {
  public enum Owns {
    IN,
    OUT,
    ERR,
    IN_OUT,
    IN_ERR,
    OUT_ERR,
    ALL,
    NONE;

    public static Owns from(EnumSet<Owns> set) {
      boolean hasIn = set.contains(IN);
      boolean hasOut = set.contains(OUT);
      boolean hasErr = set.contains(ERR);

      if (hasIn && hasOut && hasErr)
        return ALL;
      if (hasIn && hasOut)
        return IN_OUT;
      if (hasIn && hasErr)
        return IN_ERR;
      if (hasOut && hasErr)
        return OUT_ERR;
      if (hasIn)
        return IN;
      if (hasOut)
        return OUT;
      if (hasErr)
        return ERR;
      return NONE;
    }

    public boolean in() {
      return this == IN || this == IN_OUT || this == IN_ERR || this == ALL;
    }

    public boolean out() {
      return this == OUT || this == IN_OUT || this == OUT_ERR || this == ALL;
    }

    public boolean err() {
      return this == ERR || this == IN_ERR || this == OUT_ERR || this == ALL;
    }

  }

  public void closeResources() throws IOException {
    if (owns.in())
      in.close();
    if (owns.out())
      out.close();
    if (owns.err())
      err.close();
  }
}
