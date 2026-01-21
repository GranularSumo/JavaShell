package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public record IoContext(BufferedReader in, PrintWriter out, PrintWriter err, Owns owns) {
  public enum Owns {
    IN,
    OUT,
    ERR,
    OUT_ERR,
    ALL,
    NONE,
  }

  public void closeResources() throws IOException {
    switch (owns) {
      case IN: {
        in.close();
        break;
      }
      case OUT: {
        out.close();
        break;
      }
      case ERR: {
        err.close();
        break;
      }
      case OUT_ERR: {
        out.close();
        err.close();
        break;
      }
      case ALL: {
        in.close();
        out.close();
        err.close();
        break;
      }
      case NONE:
        break;
    }

  }
}
