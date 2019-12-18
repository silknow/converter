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
    this.DATASET_NAME = "MET";

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

    id = file.getName().replace(".json", "");

    //String museumName = s.get("MUSEUM");

    String regNum = s.get("Accession Number:");
    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Accession Number:"));
    obj.addTitle(s.getMulti("title").findFirst().orElse(null));

    // Image img = new Image();
    // img.setContentUrl(s.get("image"));
    // img.setContentUrl("http://silknow.org/silknow/media/met-museum/" + s.get("image").substring(s.get("image").lastIndexOf('/') + 1));
    // obj.add(img);


    // Image rimg = new Image();
    // rimg.setContentUrl(s.get("regularImage"));
    // rimg.setContentUrl("http://silknow.org/silknow/media/met-museum/" + s.get("regularImage").substring(s.get("regularImage").lastIndexOf('/') + 1));
    // obj.add(rimg);


    // Image limg = new Image();
    // limg.setContentUrl(s.get("largeImage"));
    // limg.setContentUrl("http://silknow.org/silknow/media/met-museum/" + s.get("largeImage").substring(s.get("largeImage").lastIndexOf('/') + 1));
    // obj.add(limg);


    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .forEach(image -> {
              String filename = image.getContentUrl().substring(image.getContentUrl().lastIndexOf('/') + 1);
              if (filename.isEmpty()) return; // workaround for issue #38
              image.setContentUrl("http://silknow.org/silknow/media/met-museum/" + filename);
              this.linkToRecord(image);
            });


    Production prod = new Production(regNum);
    prod.add(obj);


    s.getMulti("Date:").forEach(prod::addTimeAppellation);
    s.getMulti("Medium:").forEach(material -> prod.addMaterial(material, mainLang));


    //  y = y.replaceAll("*\\\\(.+?\\\\)", " ");


    s.getMulti("Object Type / Material").forEach(material -> prod.addMaterial(material.replaceAll(" *\\(.+?\\)", ""), mainLang));


    for (String x : s.getMulti("Culture:").collect(Collectors.toList())) {
      // TODO set as probable?
      x = x.replace("probably", "")
              .replace("(?)", "")
              .replace("?", "");

      // extract content in parentesys
      x = x.replaceAll("\\((.+)\\)", ", $1");

      String[] parts = x.split(",");
      // Starting from the end (more specific)
      // I put the first thing that I find on GeoNames
      for (int i = parts.length - 1; i >= 0; i--) {
        String part = parts[i].trim();
        if (i > 0 && GeoNames.query(part) == null)
          continue;
        prod.addPlace(part);
        break;
      }
    }
    s.getMulti("Classification:")
            .map(x -> obj.addClassification(x, "Classification", mainLang))
            .forEach(this::linkToRecord);


    String dim = s.get("Dimensions:");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("description"), "Description", "en"));

    String acquisitionFrom = s.get("Credit Line:");
    String acquisitionType = s.get("Provenance");
    LegalBody museum = null;


    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setType(acquisitionType);


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);


    Collection collection = new Collection(regNum);
    collection.of(obj);
    collection.addAppellation(s.getMulti("Department").findFirst().orElse(null));


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
