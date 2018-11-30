package org.silknow.converter.commons;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Utils {
  @Contract("_ -> new")
  @NotNull
  public static List<String> extractBrackets(@NotNull String txt) {
    if (!containsBrackets(txt)) return Arrays.asList(txt, null);

    int start = txt.indexOf("("), end = txt.indexOf(")");
    String content = txt.substring(start + 1, end);
    txt = txt.substring(0, start) + txt.substring(end + 1);
    return Arrays.asList(txt.trim(), content.trim());
  }

  public static boolean containsBrackets(@NotNull String txt) {
    return txt.contains("(") && txt.contains(")");
  }
}
