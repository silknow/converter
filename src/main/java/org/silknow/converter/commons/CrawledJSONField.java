package org.silknow.converter.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class CrawledJSONField {
  private String label;
  private String value;

  boolean hasLabel(String label) {
    return label.equalsIgnoreCase(this.label);
  }

  boolean isNotNull() {
    return !isNull();
  }

  private boolean isNull() {
    return value == null || value.isBlank() || value.equals("-");
  }

  @NotNull
  String getValue() {
    return value.replaceFirst("-$", "").trim();
  }

  Stream<String> getMultiValue(String multiSeparator) {
    return Arrays.stream(value.split(multiSeparator))
            .map(String::trim)
            .map(x -> x.replaceFirst("^/ +", ""))
            .filter(x -> !x.isBlank());
  }
}
