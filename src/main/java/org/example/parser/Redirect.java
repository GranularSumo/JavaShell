package org.example.parser;

public record Redirect(RedirectType type, Word target) {
  public Redirect {
    if (type == null || target == null) {
      throw new IllegalArgumentException("Redirect type and target cannot be null");
    }
  }

  public boolean isAppendMode() {
    return (type == RedirectType.OUTPUT_APPEND || type == RedirectType.ERROR_APPEND || type == RedirectType.ALL_APPEND);
  }
}
