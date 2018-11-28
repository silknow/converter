package org.silknow.converter.commons;

import net.sf.junidecode.Junidecode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ConstructURI {
  private static final String BASE = "http://data.silknow.org/";

  @NotNull
  public static String build(String db, String className, String identifier) {
    String seed = db + identifier + className;
    return BASE + getCollectionName(className) + "/" + generateUUID(seed);
  }

  @NotNull
  public static String build(String className, String name) {
    String seed = norm(name) + className;
    return BASE + getCollectionName(className) + "/" + generateUUID(seed);
  }

  private static String generateUUID(@NotNull String seed) {
    // source: https://gist.github.com/giusepperizzo/630d32cc473069497ac1
    try {
      String hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1")
              .digest(seed.getBytes(StandardCharsets.UTF_8)));
      UUID uuid = UUID.nameUUIDFromBytes(hash.getBytes());
      return uuid.toString();
    } catch (NoSuchAlgorithmException nsae) {
      // cannot happens
      nsae.printStackTrace();
      return "";
    }
  }

  @NotNull
  @Contract(pure = true)
  private static String getCollectionName(@NotNull String className) {
    switch (className) {
      case "ManMade_Object":
        return "object";
      case "Document":
        return "document";
      case "Image":
        return "image";
      case "LegalBody":
        return "organization";
      case "Transfer":
      case "Move":
      case "Acquisition":
        return "event";
      case "ConditionAssestment":
        return "assestment";
      default:
        return className.toLowerCase();
    }
  }

  private static String norm(String input) {
    // remove punctuation
    String seed = input.replaceAll("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()]", " ");
    // ascii transliteration
    seed = Junidecode.unidecode(seed);
    return seed;
  }


}