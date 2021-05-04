package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;

public class EuropeanaConverter extends Converter {


  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("EuropeanaConverter require files in JSON format.");

    String mainLang = "en";
    this.DATASET_NAME = "europeana";

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


    String regNum = s.getMulti("Identifier").skip(1).findFirst().orElse(null);
    if (regNum == null)
      regNum = s.getId();
    id = s.getId();

    ManMade_Object obj = new ManMade_Object(regNum);
    s.getMulti("title")
      .map(x -> obj.addClassification(x, "title", mainLang))
      .forEach(this::linkToRecord);
    s.getMulti("Subject")
      .map(x -> obj.addClassification(x, "Subject", mainLang))
      .forEach(this::linkToRecord);
    s.getMulti("Type of object")
      .map(x -> obj.addClassification(x, "Type of object", mainLang))
      .forEach(this::linkToRecord);
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));


    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("europeana"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("Date").forEach(prod::addTimeAppellation);
    s.getMulti("Creation date").forEach(prod::addTimeAppellation);

    s.getMulti("Medium").forEach(material -> prod.addMaterial(material.split(",")[0], mainLang));

    linkToRecord(obj.addObservation(s.get("description"), "Description", "en"));

    LegalBody legalbody = new LegalBody(s.getMulti("Providing institution").findFirst().orElse(null));


    Transfer transfer = new Transfer(regNum);
    String provider = s.get("Provider");
    if (provider != null) {
      transfer.of(obj).by(legalbody).by(s.get("Provider"));
    }

    Right copyphoto = new Right(obj.getUri() + "/image/right");
    s.getMulti("Rights statement for the media in this item (unless otherwise specified)")
      .map(x -> x.replaceFirst("© ", ""))
      .map(Actor::new)
      .forEach(copyphoto::ownedBy);

    linkToRecord(obj);

    linkToRecord(prod);
    linkToRecord(transfer);
    return this.model;
  }

  private void write(String text, String file) throws IOException {
    if (StringUtils.isBlank(text)) return;
    FileWriter fWriter = new FileWriter(file, true);
    BufferedWriter bWriter = new BufferedWriter(fWriter);
    bWriter.write("- " + text + "\n");
    bWriter.close();
    fWriter.close();
  }
}
