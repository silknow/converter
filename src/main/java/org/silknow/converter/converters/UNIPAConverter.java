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

public class UNIPAConverter extends Converter {

  private static final String DIMENSION_REGEX = "cm (\\d+(?:\\.\\d+)?) x (\\d+(?:\\.\\d+)?)";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  private static final String DIMENSION_REGEX2 = "(\\d+(?:\\.\\d+)?)x(\\d+(?:\\.\\d+)?)";
  private static final Pattern DIMENSION_PATTERN2 = Pattern.compile(DIMENSION_REGEX2);

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
    this.DATASET_NAME = "unipa";

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
    id = file.getName().replace(".json", "");

    String museumName = s.get("Museum");
    String regNum = s.getId();
    ManMade_Object obj = new ManMade_Object(regNum);
    //s.getMulti("title").forEach(obj::addTitle);
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));



    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("Description").findFirst().orElse(null)));
    terms.add((s.getMulti("Time chronology").findFirst().orElse(null)));
    terms.add((s.getMulti("date").findFirst().orElse(null)));
    if (s.get("Region production") != null) {
      if (s.get("Region production").equals("ignoto")) {
        terms.add(s.get("Geography"));
      }
      if (!s.get("Region production").equals("ignoto")) {
        terms.add(s.get("Region production"));
      }
    }
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);




    if (s.get("Region production") == null) {
      s.getImages().map(Image::fromCrawledJSON)
              .peek(image -> image.addInternalUrl("unipa"))
              .peek(obj::add)
              .forEach(this::linkToRecord);
    }
    Production prod = new Production(id);
    prod.add(obj);

    s.getMulti("Time chronology").forEach(prod::addTimeAppellation);

    if (s.get("Region production") != null) {
    if (s.get("Region production").equals("ignoto")) {
    prod.addPlace(s.get("Geography"));}
    if (!s.get("Region production").equals("ignoto")) {
    prod.addPlace(s.get("Region production")); }

      s.getImages().map(Image::fromCrawledJSON)
              .peek(img-> img.setLocalFilename(img.getId()))
              .peek(image -> image.addInternalUrl("unipa"))
              .peek(obj::add)
              .forEach(this::linkToRecord);

    }

    s.getMulti("Technique").forEach(technique -> prod.addTechnique(technique, mainLang));





    s.getMulti("Domaine")
            .map(x -> obj.addClassification(x, "Domain", "en"))
            .forEach(this::linkToRecord);
    s.getMulti("Appellation")
            .map(x -> obj.addClassification(x, "Appellation", "en"))
            .forEach(this::linkToRecord);
    s.getMulti("Description")
            .map(x -> obj.addClassification(x, "Description", "en"))
            .forEach(this::linkToRecord);
    

    String dim = s.getMulti("Dimensions").findFirst().orElse(null);
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }
    linkToRecord(obj.addObservation(s.getMulti("Width").findFirst().orElse(null), "Width", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Pattern ratio").findFirst().orElse(null), "Pattern ratio", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Warp").findFirst().orElse(null), "Warp", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Weft").findFirst().orElse(null), "Weft", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Construction").findFirst().orElse(null), "Construction", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Description of the pattern").findFirst().orElse(null), "Description of the pattern", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Historical Critical Information").findFirst().orElse(null), "Historical Critical Information", "it"));
    linkToRecord(obj.addObservation(s.getMulti("Pattern unit").findFirst().orElse(null), "Pattern unit", "it"));


    prod.addActivity(s.getMulti("Autors").findFirst().orElse(null), "Artist");


    s.getMulti("Author of the technical analysis").forEach(author -> obj.addActivity(new Actor (author), "Technical Analysis"));
    s.getMulti("Author of the Historical Critical Information").forEach(author -> obj.addActivity(new Actor(author), "Historical Critical Information"));


    //
    //if (museumName != null)



    ////////////////additional mappings for new records
    s.getMulti("name")
            .map(x -> obj.addClassification(x, "name", "en"))
            .forEach(this::linkToRecord);

    s.getMulti("technique_description").forEach(technique -> prod.addTechnique(technique, mainLang));


    if (s.get("storage_location") != null) {
      Transfer transfer = new Transfer(id);
      LegalBody museum = null;
      museumName = s.get("storage_location");
      museum = new LegalBody(museumName);
      transfer.of(obj).by(museum);
      linkToRecord(transfer);
      //linkToRecord(obj.addComplexIdentifier(s.get("stock_number"), "Stock Number", museum));
    }
    //else {
      //linkToRecord(obj.addComplexIdentifier(s.get("stock_number"), "Stock Number"));
    //}

    s.getMulti("date").forEach(prod::addTimeAppellation);
    String dim2 = s.getMulti("dimensions").findFirst().orElse(null);
    if (dim2 != null) {
      Matcher matcher2 = DIMENSION_PATTERN2.matcher(dim2);
      if (matcher2.find()) {
        linkToRecord(obj.addMeasure(matcher2.group(2), matcher2.group(1)));
      }
    }

    s.getMulti("manufacturing").forEach(place -> prod.addPlace(place.replace("Manifattura", "")));




    String cdt = s.get("State of preservation");
    if (cdt != null) {
      ConditionAssessment conditionAssessment = new ConditionAssessment(regNum);
      conditionAssessment.concerns(obj);
      conditionAssessment.addCondition("Condition", cdt, "it");
      linkToRecord(conditionAssessment);
    }


    if (s.get("Language") != null) {
      InformationObject bio = new InformationObject(regNum + "l");
      bio.setType("Language", "en");
      bio.isAbout(obj);
      bio.addNote(s.get("Language"), mainLang);
      linkToRecord(bio);
    }

    if (s.get("bibliography") != null) {
      InformationObject bio = new InformationObject(regNum + "l");
      bio.setType("bibliography", "en");
      bio.isAbout(obj);
      bio.addNote(s.get("bibliography"), mainLang);
      linkToRecord(bio);
    }



    linkToRecord(obj);
    linkToRecord(prod);
    //linkToRecord(doc);





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
