package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SmithsConverter extends Converter {


  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("SmithsConverter require files in JSON format.");

    String mainLang = "en";
    this.DATASET_NAME = "smiths";

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
    String museumName = "Smithsonian";

    String regNum = s.get("Record ID");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    s.getMulti("title")
            .map(x -> obj.addClassification(x, "title", mainLang))
            .forEach(this::linkToRecord);

    s.getMulti("Type")
      .map(x -> obj.addClassification(x, "Type", mainLang))
      .forEach(this::linkToRecord);


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
            .peek(image -> image.addInternalUrl("smithsonian"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("freetextdate").forEach(prod::addTimeAppellation);
    s.getMulti("made in").forEach(prod::addPlace);
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));


    PropositionalObject record = new PropositionalObject(id + "r");
    record.setType("museum record", mainLang);
    record.isAbout(obj);
    Right copyright = new Right(record.getUri() + "/right");
    copyright.applyTo(record);
    copyright.addNote(s.get("Usage of Metadata (Object Detail Text)"));
    s.getMulti("Usage of Metadata (Object Detail Text)", ", ")
      .map(x -> x.replaceFirst("© ", ""))
      .map(LegalBody::new)
      .forEach(copyright::ownedBy);


    Right copyphoto = new Right(obj.getUri() + "/image/right");
    copyphoto.addNote(s.get("Restrictions & Rights"));
    s.getMulti("Restrictions & Rights", ", ")
      .map(x -> x.replaceFirst("© ", ""))
      .map(Actor::new)
      .forEach(copyphoto::ownedBy);

    if (s.get("Medium") != null && s.get("Medium").contains("Technique:")) {

      Stream<String> MaterialStr = Arrays.stream(s.get("Medium").split("Technique:")[0].replace("Medium","").replace(": ","").split(","));
      Stream<String>  TechniqueStr = Arrays.stream( s.get("Medium").split("Technique:")[1].split(","));
      MaterialStr.forEach(material -> prod.addMaterial(material, mainLang));
      TechniqueStr.forEach(technique -> prod.addTechnique(technique, mainLang));
    }




    linkToRecord(obj.addObservation(s.get("Description"), "Description", mainLang));



    LegalBody legalbody = null;
    legalbody = new LegalBody(s.get("freetextdatasource"));


    if (s.get("Credit Line") != null) {
      Acquisition acquisition = new Acquisition(regNum);
      String acquisitionFrom = s.get("Credit Line");
      acquisition.transfer(acquisitionFrom, obj, legalbody);
      linkToRecord(acquisition);
    }


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(legalbody);


    linkToRecord(obj);

    linkToRecord(prod);
    linkToRecord(transfer);
    return this.model;
  }


}
