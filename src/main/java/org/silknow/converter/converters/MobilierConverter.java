package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MobilierConverter extends Converter {

  private static final String DIMENSION_REGEX = "(\\d+(?:\\.\\d+)?) x (\\d+(?:\\.\\d+)?) cm";
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
    this.DATASET_NAME = "mobilier";

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
    String museumName = "Mobilier";

    String regNum = s.get("inventory_id");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "inventory_id"));
    obj.addTitle(s.getMulti("title_or_designation").findFirst().orElse(null),mainLang);
    s.getMulti("denomination")
            .map(x -> obj.addClassification(x, "denomination", mainLang))
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
            .peek(image -> image.addInternalUrl("mobilier"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("conception_year_as_text").forEach(prod::addTimeAppellation);
    //s.getMulti("cultureField").forEach(prod::addPlace);




    s.getMulti("materials").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("product_types")
            .map(x -> obj.addClassification(x, "product_types", mainLang))
            .forEach(this::linkToRecord);

    linkToRecord(obj.addMeasure(s.getMulti("length_or_diameter").findFirst().orElse(""), s.getMulti("height_or_thickness").findFirst().orElse("")));



    linkToRecord(obj.addObservation(s.get("description"), "description", mainLang));
    linkToRecord(obj.addObservation(s.get("Historique"), "Historique", mainLang));



    if (s.get("bibliography") != null) {
      InformationObject bio = new InformationObject(regNum + "b");
      bio.setType("bibliography", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("bibliography"), mainLang);
      linkToRecord(bio);
    }

    Right copyphoto = new Right(obj.getUri() + "/image/right");
    copyphoto.addNote(s.get("Photographie"));
    s.getMulti("Photographie", ", ")
      .map(x -> x.replaceFirst("© ", ""))
      .map(Actor::new)
      .forEach(copyphoto::ownedBy);


    linkToRecord(obj);

    linkToRecord(prod);
    return this.model;
  }


}
