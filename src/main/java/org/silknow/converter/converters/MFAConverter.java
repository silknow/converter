package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MFAConverter extends Converter {

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

    //String mainLang = file.getName().replace(".json", "");
    this.DATASET_NAME = "MFA";

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

    String museumName = "MFA Boston";

    ManMade_Object obj = new ManMade_Object(id);
    String regNum = s.get("accessionNumber");
    linkToRecord(obj.addComplexIdentifier(regNum, "accessionNumber"));
    s.getMulti("title").forEach(obj::addTitle);

    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .forEach(this::linkToRecord);


    Production prod = new Production(id);
    prod.add(obj);

    String[] teas = s.get("teaser").split("(?=[0-9])", 2);
    if (teas.length > 1) {
      prod.addPlace(teas[0]);
      prod.addTimeAppellation(teas[1]);
    }






    s.getMulti("mediumOrTechnique").forEach(prod::addMaterial);
    s.getMulti("classifications")
            .map(x -> obj.addClassification(x, "classifications"))
            .forEach(this::linkToRecord);



    String dim = s.get("dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("description"), "en", "description"));




    LegalBody museum = null;
    if (museumName != null)
      museum = new LegalBody(museumName);


    Acquisition acquisition = new Acquisition(id);

    String[] acquisitionFrom = s.get("creditLine").split("(?<=Gift)", 2);
    if (acquisitionFrom.length > 1) {
      acquisition.setType(acquisitionFrom[0]);
      acquisition.transfer(acquisitionFrom[1], obj, museum);
    }

    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    Collection collection = new Collection(id);
    collection.of(obj);
    collection.addAppellation(s.getMulti("collections").findFirst().orElse(null));

    linkToRecord(collection);
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
