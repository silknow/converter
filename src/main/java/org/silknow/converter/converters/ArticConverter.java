package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;


import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticConverter extends Converter {

  private static final String DIMENSION_REGEX = "(\\d+(?:\\.\\d+)?) × (\\d+(?:\\.\\d+)?) cm";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("Artic converter requires files in JSON format.");

    String mainLang = "en";
    this.DATASET_NAME = "artic";

    // Parse JSON
    logger.trace("parsing json");
    CrawledJSON s;
    try {
      s = CrawledJSON.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }

    // Create the objects of the graph
    logger.trace("creating objects");

    filename = file.getName();

    String museumName = "Art Institute of Chicago (ARTIC)";

    String regNum = s.get("Reference Number");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    //obj.addTitle(s.getMulti("title").findFirst().orElse(null));
    s.getMulti("Title")
            .map(x -> obj.addClassification(x, "Title", mainLang))
            .forEach(this::linkToRecord);



    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("artic"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);


    prod.addTimeAppellation(s.getMulti("Description").findFirst().orElse(null));
    prod.addPlace(s.getMulti("Description").skip(1).findFirst().orElse(null));
    s.getMulti("Date").forEach(prod::addTimeAppellation);
    s.getMulti("Origin").forEach(prod::addPlace);
    linkToRecord(obj.addObservation(s.get("Medium"), "Medium", mainLang));
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));


    s.getMulti("Classification:").forEach(x -> obj.addClassification(x.replaceAll(" *\\(.+?\\)", ""), "Classification",  mainLang));


    String dim = s.get("Dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    String acquisitionFrom = s.get("Credit Line");
    LegalBody museum = new LegalBody(museumName);

    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.addActor(new Actor(acquisitionFrom));
    //acquisition.setType(acquisitionType);


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);


    AtomicInteger Pcounter = new AtomicInteger();

    if (s.getMulti("publication-history").findAny() != null) {

      s.getMulti("publication-history")
        .forEach(x -> {

                       InformationObject pub = new InformationObject(regNum + "_p_" + Pcounter.getAndIncrement());
                       pub.setType("Publication History", mainLang);
                       pub.isAbout(obj);
                       pub.addNote(x, mainLang);
                       linkToRecord(pub);
    });}


    AtomicInteger Ecounter = new AtomicInteger();

    if (s.getMulti("exhibition-history").findAny() != null) {

      s.getMulti("exhibition-history")
        .forEach(y -> {

          InformationObject exh = new InformationObject(regNum + "_e_" + Ecounter.getAndIncrement());
          exh.setType("Exhibition History", mainLang);
          exh.isAbout(obj);
          exh.addNote(y, mainLang);
          linkToRecord(exh);
        });}





    linkToRecord(obj);
    linkToRecord(acquisition);
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
