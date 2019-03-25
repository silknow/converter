package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CrawledJSONField {
  private String label;
  private String value;
  private List<String> values;

  boolean hasLabel(String label) {
    return label.equalsIgnoreCase(this.label);
  }

  boolean isNotNull() {
    return !isNull();
  }

  private boolean isNull() {
    return StringUtils.isBlank(value) || value.equals("-");
  }

  @NotNull
  String getValue() {
    return value.replaceFirst("-$", "").trim();
  }

  Stream<String> getMultiValue(String multiSeparator) {
    if(values != null && !values.isEmpty()) return values.stream();
    return Arrays.stream(value.split(multiSeparator))
            .map(String::trim)
            .map(x -> x.replaceFirst("^/ +", ""))
            .filter(x -> !StringUtils.isBlank(x));
  }
}
