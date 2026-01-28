package org.example.parser;

public record Word(String content) {
  public Word {
    if (content == null || content.isEmpty()) {
      throw new IllegalArgumentException("Word content cannot be null or empty");
    }
  }
}
