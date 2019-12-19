package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VAMConverter extends Converter {

  private static final String DIMENSION_REGEX = "Length: (\\d+(?:\\.\\d+)?) cm, Width: (\\d+(?:\\.\\d+)?) cm";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("VAMconverter require files in JSON format.");

    String mainLang = "en";
    this.DATASET_NAME = "VAM";

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

    id = file.getName().replace(".json", "");

    //String museumName = s.get("MUSEUM");

    String regNum = s.get("museum_number");
    ManMade_Object obj = new ManMade_Object(regNum);

    linkToRecord(obj.addComplexIdentifier(regNum, "museum_number"));
    obj.addTitle(s.getMulti("object").findFirst().orElse(null));



    Production prod = new Production(regNum);
    prod.add(obj);
    prod.addActivity(s.get("artist"), "artist");

    s.getMulti("date_text").forEach(prod::addTimeAppellation);
    s.getMulti("materials_techniques").forEach(materials -> {
      String[] mats = materials.split(";");
      for (String mat : mats) prod.addMaterial(mat, mainLang);
    });
    s.getMulti("materials").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("techniques").forEach(technique -> prod.addTechnique(technique, mainLang));


    s.getMulti("place").forEach(prod::addPlace);
    s.getMulti("categories")
            .map(x -> obj.addClassification(x, "Categories", mainLang))
            .forEach(this::linkToRecord);

    String dim = s.get("dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
    }


    linkToRecord(obj.addObservation(s.get("physical_description"), "Physical description", mainLang));
    linkToRecord(obj.addObservation(s.get("descriptive_line"), "Descriptive line", mainLang));
    linkToRecord(obj.addObservation(s.get("public_access_description"), "Summary", mainLang));
    linkToRecord(obj.addObservation(s.get("label"), "Labels and date", mainLang));



    LegalBody museum = null; // FIXME ?


    Acquisition acquisition = new Acquisition(regNum);
    acquisition.addNote(s.get("history_note"),mainLang);


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);

    Move move = new Move(regNum);
    move.of(obj).from(s.getMulti("location").findFirst().orElse(null)).to(s.getMulti("location").findFirst().orElse(null));

    Collection collection = new Collection(regNum);
    collection.of(obj);
    collection.addAppellation(s.getMulti("collections").findFirst().orElse(null));

    linkToRecord(collection);

    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .peek(image -> image.addInternalUrl("vam"))
            .forEach(this::linkToRecord);

    if (s.get("bibliography") != null) {
      InformationObject bio = new InformationObject(regNum + "b");
      bio.setType("Bibliography", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("Bibliography"),mainLang);
      linkToRecord(bio);
    }

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
