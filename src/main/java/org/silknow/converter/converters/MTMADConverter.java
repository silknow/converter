package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MTMADConverter extends Converter {

  private static final String DIMENSION_REGEX = "H. (\\d+(?:[,.]\\d+)?) cm , l. (\\d+(?:[,.]\\d+)?) cm";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("MTMADconverter require files in JSON format.");

    //String mainLang = file.getName().replace(".json", "");
    this.DATASET_NAME = "MTMAD";

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
    String regNum = s.getId();
    linkToRecord(obj.addComplexIdentifier(regNum, id));
    obj.addTitle(s.getMulti("title").findFirst().orElse(null));

    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(id);
    prod.add(obj);

    String[] details = s.getMulti("details").toArray(String[]::new);
    for(int i = 0; i < details.length; i++)
    {
      if (details[i].startsWith("H")) {
        String dim = details[i];
        if (dim != null) {
          Matcher matcher = DIMENSION_PATTERN.matcher(dim);
          if (matcher.find()) {
            linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
          }
        }
      }


    }
    linkToRecord(obj.addObservation(details[0], "fr", "short description"));




    String dim = s.getMulti("Mesures").findFirst().orElse(null);
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.getMulti("description").findFirst().orElse(null), "en", "description"));

    String acquisitionFrom = s.getMulti("Credit Line:").findFirst().orElse(null);
    String acquisitionType = s.getMulti("Acquisition/dépôt:").findFirst().orElse(null);
    LegalBody museum = null;

    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setType(acquisitionType);


    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    obj.addSubject(s.getMulti("Iconografia").findFirst().orElse(null));


    if (s.get("Bibliographie :") != null) {
      InformationObject bio = new InformationObject(regNum + "b");
      bio.setType("Bibliographie");
      bio.isAbout(obj);
      bio.addNote(s.getMulti("Bibliographie").findFirst().orElse(null));
      linkToRecord(bio);
    }

    if (s.get("Exposition :") != null) {
      InformationObject bio = new InformationObject(regNum + "e");
      bio.setType("Exhibitions");
      bio.isAbout(obj);
      bio.addNote(s.getMulti("Exposition").findFirst().orElse(null));
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
