package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersaillesConverter extends Converter {

  private static final String DIMENSION_REGEX = "H. (\\d+(?:\\,\\d+)?) ; L. (\\d+(?:\\,\\d+)?) cm.";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("MFAConverter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "versailles";

    // Parse JSON
    logger.trace("parsing json");
    CrawledJSON s;
    try {
      s = CrawledJSON.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    //s.setMultiSeparator(" -");

    // Create the objects of the graph
    logger.trace("creating objects");

    filename = file.getName();
    String museumName = "Versailles";

    String regNum = s.get("Nº d'inventaire:");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Nº d'inventaire:"));
    obj.addTitle(s.getMulti("title").findFirst().orElse(null),mainLang);
    s.getMulti("Désignation")
            .map(x -> obj.addClassification(x, "Désignation", mainLang))
            .forEach(this::linkToRecord);
    linkToRecord(obj.addProperty(OWL.sameAs, s.getUrl()));

    /*
    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("titleField").findFirst().orElse(null)));
    terms.add((s.getMulti("displayDateField").findFirst().orElse(null)));
    terms.add((s.getMulti("cultureField").findFirst().orElse(null)));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);
/*


     */
    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("versailles"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("Date de création").forEach(prod::addTimeAppellation);
    //s.getMulti("cultureField").forEach(prod::addPlace);




    s.getMulti("Matière et technique").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Domaine")
            .map(x -> obj.addClassification(x, "Domaine", mainLang))
            .forEach(this::linkToRecord);

    s.getMulti("Personne représentée").forEach(subject -> obj.addSubject(subject, mainLang));


    String dim = s.get("Dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("Historique"), "Historique", mainLang));



    linkToRecord(obj);

    linkToRecord(prod);
    return this.model;
  }


}
