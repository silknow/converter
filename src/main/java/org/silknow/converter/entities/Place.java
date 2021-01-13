package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDFS;
import org.geonames.Toponym;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.commons.GeoNames;
import org.silknow.converter.commons.StopWordException;
import org.silknow.converter.ontologies.CIDOC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Place extends Entity {
  private static final List<String> STOPWORDS = Arrays.asList(
    "desconocido", "unknown", "desconegut", "ignoto", "about", "probably");
  private static HashMap<String, String> DEMONYM = null;
  private static HashMap<String, String> IT_DEMONYM = null;

  private static final String IT_ART_REGEX = "(?i)(arte|area|ambito|manifattura) (di |dell')?";

  private static final List<String> FAR_EAST = Arrays.asList("Far East", "Extrem Orient", "Extremo Oriente");
  private static final List<String> NEAR_EAST = Arrays.asList("Near East", "Near Easter", "Pròxim Orient", "Oriente Próximo");


  public Place(String name) throws StopWordException {
    super();
    String feature_code = null;

    name = name.trim();

    if (name.length() == 0) // a place cannot have an empty name
      throw new StopWordException();
    if (STOPWORDS.contains(name.toLowerCase()))
      throw new StopWordException();
    if (name.matches("^\\d.+")) // a place cannot start with a number
      throw new StopWordException();

    name = name.replaceAll(TimeSpan.UNCERTAIN_REGEX, "");
    name = name.replaceAll("(?i)\\(?(used|marketed|worn) in .+\\)?", ""); // English, used in America
    name = name.replaceAll("(?i)\\((embroider(ed|y|ing)|used|made|published|designed (and made)?|printed|\\d+)\\)", "");
    name = name.replaceAll("(?i)\\((collected|sewing|worn|manufactured|(hand )?weaving|woven|quilted|paint(ing|ed))\\)", "");
    name = name.replaceAll("(?i)\\((retailed|joinery|sold|upholstered)\\)", "");
    name = name.replaceAll("(?i)for .+ market", ""); // English, used in America
    name = name.replaceAll("(?i)for export", "");
    name = name.replaceAll("(?i)\\[(.+)?para (?:la )?exportación.+(]|$)", "");
    name = name.replaceAll(IT_ART_REGEX, "");

    // Geonames is not good with continents
    String continent = null;
    if (name.contains("Europa") && name.length() > 6) {
      continent = "+EU";
      name = name.replaceAll("Europa", "").trim();
    } else if (name.contains("Asia)")) {
      continent = "+AS";
      name = name.replaceAll("Asia", "").trim();
    }

    if (name.contains("(island)")) {
      name = name.replace("(island)", "");
      feature_code = "ISL";
    } else if (name.contains("(city)")) {
      name = name.replace("(city)", "");
      feature_code = "P";
    }

    name = name.trim().replaceAll(",$", "");
    name = name.replaceAll("\\( *\\)", "")
      .replaceAll("\\[ ?]", "")
      .replaceAll("^\\[(.+)]", "$1")
      .replaceAll(", ([)\\]])", "$1")
      .replaceAll("\\( ", "(")
      .replaceAll("\\s+", " ")
      .replaceAll("\\.$f", " ")
      .trim();

    // if it is a Demonym, I convert it to a place
    name = Place.fromDemonym(name);
    name = Place.fromDemonymIT(name);

    // final trim before searching
    name = name.trim();

    Toponym tp = GeoNames.query(name, feature_code, continent);
    if (tp != null) {
      GeoNames.downloadRdf(tp.getGeoNameId());
      this.setUri(GeoNames.toURI(tp.getGeoNameId()));
      this.addProperty(RDFS.label, tp.getName());

      this.setClass(CIDOC.E53_Place);
      return;
    }

    // case Italian (Florence) or Spanish, Almeria
    tp = parseDemoCity(name, continent);

    if (tp != null) {
      GeoNames.addToCache(name, tp.getGeoNameId());
      GeoNames.downloadRdf(tp.getGeoNameId());
      this.setUri(GeoNames.toURI(tp.getGeoNameId()));
      this.addProperty(RDFS.label, tp.getName());

      this.setClass(CIDOC.E53_Place);
      return;
    }

    String seed = name;
    // far east interlinking workaround
    if (FAR_EAST.contains(name)) seed = FAR_EAST.get(0);
    if (NEAR_EAST.contains(name)) seed = NEAR_EAST.get(0);

    this.setUri(ConstructURI.build(this.className, seed));
    this.addProperty(RDFS.label, name).addProperty(CIDOC.P87_is_identified_by, name);

    this.setClass(CIDOC.E53_Place);
  }

  private static final String BRACKETS_FORMAT = "(.+) [(\\[](.+)[)\\]]";
  private static final Pattern BRACKETS_FORMAT_PATTERN = Pattern.compile(BRACKETS_FORMAT);

  private Toponym parseDemoCity(String name, String continent) {
    name = name.replaceAll("(?i)^Greek islands", "Greece");

    String p1, p2;
    if (name.matches(BRACKETS_FORMAT)) {
      Matcher m = BRACKETS_FORMAT_PATTERN.matcher(name);
      m.find();
      p1 = m.group(1);
      p2 = m.group(2);
      if (p2.contains(", ")) {
        String[] parts = p2.split(", ");
        p2 = parts[parts.length - 1];
      }
    } else if (name.contains("[,:] ")) {
      String[] parts = name.split("[,:] ");
      p1 = parts[0];
      p2 = parts[parts.length - 1];
    } else return null;

    Toponym t1;
    String place1 = fromDemonym(p1);
    String place2 = fromDemonym(p2);

    if ("English".equalsIgnoreCase(p1)) place1 = "United Kingdom"; // workaround
    else if ("Irish".equalsIgnoreCase(p1)) place1 = "Republic of Ireland"; // workaround
    else if ("Flemish".equalsIgnoreCase(p1)) {// workaround
      t1 = GeoNames.query(place2, null, continent, true);
      if (t1 != null && t1.getCountryCode().matches("(FR|NL|BE)"))
        return t1;
      else return null;
    }

    t1 = GeoNames.query(place1, "PCLI", continent, true);
    if (t1 == null) {
      t1 = GeoNames.query(place2, "PCLI", continent, true);
      place2 = place1;
    }
    if (t1 == null) return null;

    return GeoNames.query(place2, null, t1.getCountryCode(), true);
  }

  public Place(Toponym tp) {
    super();
    this.setUri(GeoNames.toURI(tp.getGeoNameId()));

    GeoNames.downloadRdf(tp.getGeoNameId());
    this.setClass(CIDOC.E53_Place);
    this.addProperty(RDFS.label, tp.getName());
  }

  public static String fromDemonym(String demonym) {
    if (DEMONYM == null) DEMONYM = loadDemonymMap("en");
    return DEMONYM.getOrDefault(demonym, demonym);
  }

  public static String fromDemonymIT(String demonym) {
    if (IT_DEMONYM == null) IT_DEMONYM = loadDemonymMap("it");
    return IT_DEMONYM.getOrDefault(demonym, demonym);
  }

  private static HashMap<String, String> loadDemonymMap(String lang) {
    HashMap<String, String> dmn = new HashMap<>();
    ClassLoader classLoader = Place.class.getClassLoader();
    InputStream str = Objects.requireNonNull(classLoader.getResourceAsStream("demonym_" + lang + ".csv"));
    BufferedReader br = new BufferedReader(new InputStreamReader(str));
    try {
      while (br.ready()) {
        String[] line = br.readLine().split(",");
        dmn.put(line[0], line[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return dmn;
  }

  //public String getLabel() {return this.resource.getProperty(RDFS.label).getObject().toString();}


}
