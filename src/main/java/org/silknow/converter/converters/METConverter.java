package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.commons.GeoNames;
import org.silknow.converter.entities.*;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class METConverter extends Converter {

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
      throw new RuntimeException("MET converter requires files in JSON format.");

    String mainLang = "en";
    this.DATASET_NAME = "met";

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

    //String museumName = s.get("MUSEUM");

    String regNum = s.get("Accession Number:");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Accession Number:"));
    //obj.addTitle(s.getMulti("title").findFirst().orElse(null));
    s.getMulti("title")
            .map(x -> obj.addClassification(x, "Categories", mainLang))
            .forEach(this::linkToRecord);


    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("met-museum"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("Date:").forEach(prod::addTimeAppellation);
    s.getMulti("Medium:").forEach(material -> prod.addMaterial(material, mainLang));

    //s.getMulti("Object Type / Material").forEach(material -> prod.addMaterial(material.replaceAll(" *\\(.+?\\)", ""), mainLang));

    s.getMulti("Object Type / Material").forEach(x -> obj.addClassification(x.replaceAll(" *\\(.+?\\)", ""), "Object Type / Material",  mainLang));




    //s.getMulti("Culture:").forEach(prod::addPlace);

    // TODO make these rules general!
    for (String x : s.getMulti("Culture:").collect(Collectors.toList())) {
      x = x.replace("probably", "")
              .replace("(?)", "")
              .replace("?", "");

      // extract content in parentheses
      x = x.replaceAll("\\((.+)\\)", ", $1");

      String[] parts = x.split(",");
      // Starting from the end (more specific)
      // I put the first thing that I find on GeoNames
      for (int i = parts.length - 1; i >= 0; i--) {
        String part = Place.fromDemonym(parts[i].trim());
        if (i > 0 && GeoNames.query(part) == null)
          continue;
        prod.addPlace(part);
        break;
      }
    }


    s.getMulti("Classification:").forEach(x -> obj.addClassification(x.replaceAll(" *\\(.+?\\)", ""), "Classification:",  mainLang));


    String dim = s.get("Dimensions:");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("description"), "Description", "en"));

    String acquisitionFrom = s.get("Credit Line:");
    //String acquisitionType = s.get("Provenance");
    LegalBody museum = null; // FIXME ?


    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(acquisitionFrom, obj, museum);
    //acquisition.setType(acquisitionType);


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);


    String appellation = s.getMulti("Department").findFirst().orElse(null);
    if (appellation != null) {

      Collection collection = new Collection(regNum, appellation.replaceAll(" *\\(.+?\\)", ""));
      collection.of(obj);
      collection.addAppellation(appellation.replaceAll(" *\\(.+?\\)", ""));
      linkToRecord(collection);
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
