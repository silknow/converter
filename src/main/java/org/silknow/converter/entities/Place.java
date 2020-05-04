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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Place extends Entity {
  private static final List<String> STOPWORDS = Arrays.asList(
          "desconocido", "unknown", "desconegut", "ignoto", "about", "probably");
  private static HashMap<String, String> DEMONYM = null;

  public Place(String name) throws StopWordException {
    super();

    name = name.trim();

    if (name.length() == 0) // a place cannot have an empty name
      throw new StopWordException();
    if (STOPWORDS.contains(name.toLowerCase()))
      throw new StopWordException();
    if (name.matches("^\\d.+")) // a place cannot start with a number
      throw new StopWordException();

    name = name.replaceAll("(?i)(possi|proba)bly", "");
    name = name.replaceAll("(?i)posiblemente", "");
    name = name.replaceAll("\\?", "");
    name = name.replaceAll("\\((embroider(ed|y|ing)|used|made|published|designed|printed|\\d+)\\)", "");
    name = name.replaceAll("\\((collected|sewing|worn|manufactured|(hand )?weaving|woven|quilted|paint(ing|ed))\\)", "");
    name = name.replaceAll("\\((retailed|joinery)\\)", "");

    // Geonames is not good with continents
    String continent = null;
    if (name.contains("Europa") && name.length() > 6) {
      continent = "EU";
      name = name.replaceAll("\\(?Europa\\)?", "").trim();
    } else if (name.contains("Asia)")) {
      continent = "AS";
      name = name.replaceAll("\\(?Asia\\)", "").trim();
    }

    name = name.trim().replaceAll(",$", "");
    name = name.replaceAll("\\( ?\\)", "")
            .replaceAll("\\[ ?]", "")
            .replaceAll("^\\[", "").replaceAll("]$", "")
            .trim();

    // if it is a Demonym, I convert it to a place
    name = Place.fromDemonym(name);

    // final trim before searching
    name = name.trim();

    Toponym tp = GeoNames.query(name, null, continent);
    if (tp != null) {
      GeoNames.downloadRdf(tp.getGeoNameId());
      this.setUri(GeoNames.toURI(tp.getGeoNameId()));
      this.addProperty(RDFS.label, tp.getName());
    } else {
      this.setUri(ConstructURI.build(this.className, name));
      this.addProperty(RDFS.label, name).addProperty(CIDOC.P1_is_identified_by, name);
    }

    this.setClass(CIDOC.E53_Place);
  }

  public static String fromDemonym(String demonym) {
    if (DEMONYM == null) loadDemonymMap();
    return DEMONYM.getOrDefault(demonym, demonym);
  }

  private static void loadDemonymMap() {
    DEMONYM = new HashMap<>();
    ClassLoader classLoader = Place.class.getClassLoader();
    InputStream str = Objects.requireNonNull(classLoader.getResourceAsStream("demonym.csv"));
    BufferedReader br = new BufferedReader(new InputStreamReader(str));
    try {
      while (br.ready()) {
        String[] line = br.readLine().split(",");
        DEMONYM.put(line[0], line[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
