package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UNIPAConverter extends Converter {

  private static final String DIMENSION_REGEX = "cm (\\d+(?:\\.\\d+)?) x (\\d+(?:\\.\\d+)?)";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("UNIPA converter require files in JSON format.");

    String mainLang = "it";
    this.DATASET_NAME = "UNIPA";

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

    String museumName = s.get("Museum");

    ManMade_Object obj = new ManMade_Object(id);
    String regNum = s.getId();
    linkToRecord(obj.addComplexIdentifier(regNum, id));
    //s.getMulti("title").forEach(obj::addTitle);


    s.getImages().map(Image::fromCrawledJSON)
            .peek(img -> {
              img.setContentUrl("http://silknow.org/media/unipa/" + img.getId()+".jpg");
            })
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(id);
    prod.add(obj);

    s.getMulti("Time chronology").forEach(prod::addTimeAppellation);

    s.getMulti("Geography").forEach(prod::addPlace);
    s.getMulti("Region production").forEach(prod::addPlace);
    s.getMulti("Technic").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("Domaine")
            .map(x -> obj.addClassification(x, "Domain", "en"))
            .forEach(this::linkToRecord);
    s.getMulti("Appellation")
            .map(x -> obj.addClassification(x, "Appellation", "en"))
            .forEach(this::linkToRecord);


    String dim = s.getMulti("Dimensions").findFirst().orElse(null);
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }
    linkToRecord(obj.addObservation(s.getMulti("Width").findFirst().orElse(null), "it", "Width"));
    linkToRecord(obj.addObservation(s.getMulti("Description").findFirst().orElse(null), "it", "Description"));
    linkToRecord(obj.addObservation(s.getMulti("Pattern ratio").findFirst().orElse(null), "it", "Pattern ratio"));
    linkToRecord(obj.addObservation(s.getMulti("Warp").findFirst().orElse(null), "it", "Warp"));
    linkToRecord(obj.addObservation(s.getMulti("Weft").findFirst().orElse(null), "it", "Weft"));
    linkToRecord(obj.addObservation(s.getMulti("Costruction").findFirst().orElse(null), "it", "Construction"));
    linkToRecord(obj.addObservation(s.getMulti("Description of the pattern").findFirst().orElse(null), "it", "Description of the pattern"));
    linkToRecord(obj.addObservation(s.getMulti("Historical Critical Information").findFirst().orElse(null), "it", "Historical Critical Information"));


    prod.addActivity(s.getMulti("Autors").findFirst().orElse(null), "Artist");


    LegalBody museum = null;
    if (museumName != null)
      museum = new LegalBody(museumName);




    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    String cdt = s.get("Preservation state");
    if (cdt != null) {
      ConditionAssessment conditionAssessment = new ConditionAssessment(regNum);
      conditionAssessment.concerns(obj);
      conditionAssessment.addCondition("Condition", cdt, "it");
      linkToRecord(conditionAssessment);
    }


    if (s.get("Language") != null) {
      InformationObject bio = new InformationObject(regNum + "l");
      bio.setType("Language", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("Language"), mainLang);
      linkToRecord(bio);
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
