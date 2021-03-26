package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;
import org.silknow.converter.ontologies.CIDOC;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VAMConverter extends Converter {
  private static final String DIMENSION_REGEX = "(.+): (Over |<)?(\\d+(?:\\.\\d+)?)( ?[½¾¼]| \\d/\\d)? *([a-z]{1,4})?( .+)?";
  private static final String DIMENSION_REGEX2 = "(.+): *([a-z]{1,4}) ?(\\d+(?:\\.\\d+)?)([½¾¼]| \\d/\\d)?( .+)?";
  private static final String Pattern_unit_REGEX = "Length: (\\d+(?:\\.\\d+)?) cm repeat";
  private static final Pattern Pattern_unit_PATTERN = Pattern.compile(Pattern_unit_REGEX);
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);
  private static final Pattern DIMENSION_PATTERN2 = Pattern.compile(DIMENSION_REGEX2);

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
    this.DATASET_NAME = "vam";

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

    String regNum = s.get("object_number");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "object_number"));

    s.getMulti("object").forEach(x -> obj.addClassification(x, "object", mainLang));


    Production prod = new Production(regNum);
    prod.add(obj);
    prod.addActivity(s.get("artist"), "artist");
    prod.addNote(s.get("production_note"));

    s.getMulti("date_text").forEach(prod::addTimeAppellation);
    s.getMulti("materials_techniques").forEach(materials -> {
      String[] mats = materials.split(";");
      for (String mat : mats) prod.addMaterial(mat, mainLang);
    });
    s.getMulti("materials").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("techniques").forEach(technique -> prod.addTechnique(technique, mainLang));

    s.getMulti("place").forEach(prod::addPlace);
    s.getMulti("categories")
      .map(x -> obj.addClassification(x, "categories", mainLang))
      .forEach(this::linkToRecord);

    parseDimensions(s.get("dimensions"), obj);

    linkToRecord(obj.addObservation(s.get("physical_description"), "Physical description", mainLang));

    linkToRecord(obj.addObservation(s.get("production_type"), "Production Type", mainLang));

    linkToRecord(obj.addObservation(s.get("descriptive_line"), "Descriptive Line", mainLang));

    linkToRecord(obj.addObservation(s.get("public_access_description"), "Summary", mainLang));

    linkToRecord(obj.addObservation(s.get("label"), "Labels and date", mainLang));

    linkToRecord(obj.addObservation(s.get("historical_context_note"), "Historical Context Note", mainLang));


    LegalBody museum = null; // FIXME ?


    Acquisition acquisition = new Acquisition(regNum);
    acquisition.addNote(s.get("history_note"), mainLang);


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);

    if (s.getMulti("location").findFirst().orElse(null) != null) {
      Move move = new Move(regNum);
      move.of(obj).from(s.getMulti("location").findFirst().orElse(null)).to(s.getMulti("location").findFirst().orElse(null));
    }

    Collection collection = new Collection(regNum, s.getMulti("collections").findFirst().orElse(null));
    collection.of(obj);
    collection.addAppellation(s.getMulti("collections").findFirst().orElse(null));

    linkToRecord(collection);

    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("vam"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    if (s.get("bibliography") != null) {
      InformationObject bio = new InformationObject(regNum + "b");
      bio.setType("Bibliography", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("Bibliography"), mainLang);
      linkToRecord(bio);
    }

    String dim = s.get("dimensions");
    if (dim != null) {
      Matcher matcher5 = Pattern_unit_PATTERN.matcher(dim);
      if (matcher5.find()) {
        linkToRecord(obj.addPatternUnit(matcher5.group(1)));
      }
    }

    linkToRecord(obj);
    linkToRecord(acquisition);
    linkToRecord(prod);
    linkToRecord(transfer);
    return this.model;
  }

  private void parseDimensions(String dim, ManMade_Object obj) {
    if (StringUtils.isBlank(dim) || dim.length() < 2) return;
    String dimUri = obj.getUri() + "/dimension/";

    String unit = null;
    int count = 1;
    List<Dimension> dimList = new ArrayList<>();
    for (String txt : dim.split(", (?=[A-Z][a-z])")) {
      Dimension d = parseSingleDimension(txt, unit, dimUri + count);
      if (d == null) continue;
      count++;
      unit = d.getUnit();
      dimList.add(d);
    }
    if (dimList.size() == 0) return;

    Resource measure = model.createResource(dimUri + "measurement")
      .addProperty(RDF.type, CIDOC.E16_Measurement)
      .addProperty(CIDOC.P39_measured, obj.asResource());

    for (Dimension d : dimList) {
      obj.addProperty(CIDOC.P43_has_dimension, d);
      measure.addProperty(CIDOC.P40_observed_dimension, d.asResource());
      model.add(d.getModel());
    }

    linkToRecord(measure);
  }



  private static final Map<String, String> DIM_NOTES;

  static {
    Map<String, String> map = new HashMap<>();
    map.put("repe", "repetition");
    map.put("bott", "bottom");
    map.put("maxi", "maximum");
    map.put("mini", "minimum");
    map.put("a", "a");
    DIM_NOTES = map;
  }

  private Dimension parseSingleDimension(String txt, String unit, String dimUri) {
    if (txt.length() < 2) return null;
    Matcher matcher = DIMENSION_PATTERN.matcher(txt);
    Matcher matcher2 = DIMENSION_PATTERN2.matcher(txt);

    String type, value, fraction, note, label, modifier = null;
    if (matcher.find()) {
      label = matcher.group();
      type = matcher.group(1).toLowerCase();
      modifier = matcher.group(2);
      value = matcher.group(3);
      fraction = matcher.group(4);
      note = matcher.group(6);
      String cUnit = matcher.group(5);
      if (cUnit != null) {
        unit = cUnit;
      } else if (DIM_NOTES.containsKey(cUnit)) {
        cUnit = DIM_NOTES.get(cUnit);
        if (note == null) note = cUnit;
        else note = label + " " + cUnit;
      }
    } else if (matcher2.find()) {
      label = matcher2.group();
      type = matcher2.group(1).toLowerCase();
      value = matcher2.group(3);
      fraction = matcher2.group(4);
      unit = matcher2.group(2);
      note = matcher2.group(5);
    } else return null;

    if (unit == null) return null;

    String decimal = "";
    if (fraction != null) {
      fraction = fraction.trim();
      if (fraction.contains("/")) {
        String[] parts = fraction.split("/");
        decimal = String.valueOf(Float.parseFloat(parts[0]) / Float.parseFloat(parts[1]));
        decimal = decimal.substring(1);
      } else if ("¼".equals(fraction))
        decimal = ".25";
      else if ("½".equals(fraction))
        decimal = ".5";
      else if ("¾".equals(fraction))
        decimal = ".75";
    }
    Dimension d = new Dimension(dimUri, value + decimal, unit, type, label);
    if ("Over ".equals(modifier)) d.addNote("minimum");
    if ("<".equals(modifier)) d.addNote("maximum");
    d.addNote(note);

    return d;
  }
}
