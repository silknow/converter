package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;

public class GallicaConverter extends Converter {


  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("GallicaConverter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "gallica";

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


    String regNum = s.getMulti("Identifiant").findFirst().orElse(null);
    if (regNum == null) regNum = s.getId();
    id = s.getId();

    ManMade_Object obj = new ManMade_Object(regNum);
    s.getMulti("typeDocument")
      .map(x -> obj.addClassification(x, "typeDocument", mainLang))
      .forEach(this::linkToRecord);
    obj.addTitle(s.get("Titre"), mainLang);
    if (s.getUrl() != null) {
      linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl()))); }

    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("gallica"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("Date d'édition").forEach(prod::addTimeAppellation);
    s.getMulti("Langue").forEach(prod::addPlace);


    s.getMulti("notice.Matière et technique de production :").forEach(material -> prod.addMaterial(material.split(",")[0], mainLang));



    s.getMulti("Description")
      .map(x -> obj.addObservation(x, "Description", mainLang))
      .forEach(this::linkToRecord);
    s.getMulti("book.Note(s) ")
      .map(x -> obj.addObservation(x, "book.Note(s) ", mainLang))
      .forEach(this::linkToRecord);


    LegalBody legalbody = new LegalBody(s.get("Provenance"));


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(legalbody);

    String sj = s.getMulti("Droits").findFirst().orElse(null);
    if (sj != null) {
      Right right = new Right(obj.getUri() + "/right");
      right.addNote(sj, mainLang);
      right.applyTo(obj);
      linkToRecord(right);
    }

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
