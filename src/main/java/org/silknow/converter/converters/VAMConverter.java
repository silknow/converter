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

    ManMade_Object obj = new ManMade_Object(id);
    String regNum = s.get("museum_number");
    linkToRecord(obj.addComplexIdentifier(regNum, "museum_number"));
    obj.addTitle(s.getMulti("object").findFirst().orElse(null));



    Production prod = new Production(id);
    prod.add(obj);
    prod.addActivity(s.get("artist"), "artist");

    s.getMulti("date_text").forEach(prod::addTimeAppellation);
    s.getMulti("materials_techniques").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("place").forEach(prod::addPlace);
    //s.getMulti("TÈCNICA*").forEach(prod::addTechnique);
    s.getMulti("categories")
            .map(x -> obj.addClassification(x, "Categories", mainLang))
            .forEach(this::linkToRecord);
    //s.getMulti("DENOMINACIÓ*")
      //      .map(x -> obj.addClassification(x, "domain"))
        //    .forEach(this::linkToRecord);
    //s.getMulti("DESTÍ DÚS*").forEach(obj::addIntention);



    String dim = s.get("dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("physical_description"), "en", "physical_description"));
    linkToRecord(obj.addObservation(s.get("descriptive_line"), "en", "descriptive_line"));
    linkToRecord(obj.addObservation(s.get("public_access_description"), "en", "summary"));



    LegalBody museum = null;


    Acquisition acquisition = new Acquisition(id);



    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    Move move = new Move(id);
    move.of(obj).from(s.getMulti("location").findFirst().orElse(null)).to(s.getMulti("location").findFirst().orElse(null));

    Collection collection = new Collection(id);
    collection.of(obj);
    collection.addAppellation(s.getMulti("collections").findFirst().orElse(null));

    linkToRecord(collection);

    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .forEach(image -> {
              image.setContentUrl("http://silknow.org/silknow/media/vam/" + image.getContentUrl().substring(image.getContentUrl().lastIndexOf('/') + 1));
              this.linkToRecord(image);
            });

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
