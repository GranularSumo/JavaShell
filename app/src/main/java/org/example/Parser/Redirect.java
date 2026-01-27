package org.example.Parser;

public record Redirect(RedirectType type, Word target) {
  public Redirect {
    if (type == null || target == null) {
      throw new IllegalArgumentException("Redirect type and target cannot be null");
    }
  }
}
