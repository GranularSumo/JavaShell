package org.example.Parser;

import java.util.HashMap;
import java.util.Map;

public enum RedirectType {
  OUTPUT(">", "1>"),
  OUTPUT_APPEND(">>", "1>>"),
  ERROR("2>"),
  ERROR_APPEND("2>>"),
  ALL_OUTPUT("&>"),
  ALL_APPEND("&>>"),
  INPUT("<");

  private final String[] symbols;
  private static final Map<String, RedirectType> LOOKUP = new HashMap<>();

  static {
    for (RedirectType type : values()) {
      for (String symbol : type.symbols) {
        LOOKUP.put(symbol, type);
      }
    }
  }

  private RedirectType(String... symbols) {
    this.symbols = symbols;
  }

  public static RedirectType fromString(String value) {
    RedirectType type = LOOKUP.get(value);
    if (type == null) {
      throw new IllegalArgumentException("Unknown redirect: " + value);
    }
    return type;
  }

  public static boolean isRedirectPrefix(char c) {
    return c == '>' || c == '<' || c == '1' || c == '2' || c == '&';
  }

  public static boolean isValidRedirectType(String redirect) {
    return LOOKUP.containsKey(redirect);
  }
}
