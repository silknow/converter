package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawledJSONField {
  private String label;
  private String value;
  private List<String> values;


  public CrawledJSONField() {
  }

  boolean hasLabel(String label) {
    return label.equalsIgnoreCase(this.label);
  }

  boolean isNotNull() {
    return !isNull();
  }

  private boolean isNull() {
    if(values != null && !values.isEmpty()) return false;
    return StringUtils.isBlank(value) || value.equals("-");
  }

  @NotNull
  String getValue() {
    return value.replaceFirst("-$", "").trim();
  }

  Stream<String> getMultiValue(String multiSeparator) {
    if(values != null && !values.isEmpty()) return values.stream().filter(x-> !x.replace('\u00A0',' ').trim().isEmpty());
    return Arrays.stream(value.split(multiSeparator))
            .map(String::trim)
            .map(x -> x.replaceFirst("^/ +", ""))
            .filter(x -> !StringUtils.isBlank(x));
  }

  Stream<String> getMultiValue() {
    if(values != null && !values.isEmpty())
      return values.stream().filter(x-> !x.replace('\u00A0',' ').trim().isEmpty());

    return null;
  }


}
