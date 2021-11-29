package org.silknow.converter.converters;

import com.google.gson.Gson;
import org.silknow.converter.commons.CrawledJSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PMRecord extends CrawledJSON {
  private PMRaw raw;

  public Stream<List<String>> getAuthors() {
    return raw.fieldOeuvreAuteurs.stream().map(this::parseAuteur);
  }

  private List<String> parseAuteur(Auteur aut) {
    InnerEntity author = aut.entity.fieldAuteurAuteur.entity;
    if ((author.name != null) && (author.fieldPipDateNaissance != null) && (author.fieldPipDateDeces != null)){
    return Arrays.asList(author.name,
      author.fieldPipDateNaissance.startYear.toString(),
      author.fieldPipDateDeces.startYear.toString()); }
    else
      return null;
  }

  private class PMRaw {
    private List<Auteur> fieldOeuvreAuteurs;
  }

  private class Auteur {
    private AuteurEntity entity;

  }

  private class AuteurEntity {
    private InnerAuteur fieldAuteurAuteur;
  }

  private class InnerAuteur {
    private InnerEntity entity;
  }

  private class InnerEntity {
    private String name;
    private AuteurDate fieldPipDateNaissance;
    private AuteurDate fieldPipDateDeces;
  }

  private class AuteurDate {
    private Integer startYear;
  }

  public static PMRecord from(File file) throws FileNotFoundException {
    return new Gson().fromJson(new FileReader(file), PMRecord.class);
  }
}

