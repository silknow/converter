package org.silknow.converter.entities;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.ontologies.CIDOC;

import java.util.Arrays;

public class Inscription extends Entity {
  public Inscription() {
    super();
    this.resource = model.createResource();
    this.setClass(CIDOC.E34_Inscription);
  }

  public static Inscription fromJoconde(@NotNull String text, String lang) {
    Inscription inscription = new Inscription();
    String[] parts = text.split(":");


    for (int i = 0; i < parts.length; i++) {
      String part = parts[i].trim();
      if (part.isBlank()) continue;
      if (i == 0) {
        Arrays.asList(part.split(","))
                .forEach(x -> {
                  if (x.matches("(en|au) .+")) inscription.addNote(x, "fr");
                  else inscription.addType(x);
                });
      } else if (i > 3 || i == parts.length - 1 || isProbablyInital(part)) {
        // real inscription text
        inscription.addText(part.replaceAll("/", "\n"), lang);
      } else inscription.addNote(part, "fr");
    }
    return inscription;
  }

  private static boolean isProbablyInital(String txt) {
    txt = txt.substring(0, 2).replace(".", "");
    return StringUtils.isAllUpperCase(txt);
  }

  private void addText(String txt, String lang) {
    this.addNote("\"" + txt + "\"", lang);
  }
}
