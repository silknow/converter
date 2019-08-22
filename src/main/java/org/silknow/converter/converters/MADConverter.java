package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MADConverter extends Converter {

  private static final String DIMENSION_REGEX = "hauteur en cm : (\\d+?) largeur en cm : (\\d+?) ";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("MADconverter require files in JSON format.");

    this.DATASET_NAME = "MAD";

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


    ManMade_Object obj = new ManMade_Object(id);
    String regNum = s.getMulti("Numéro d'inventaire:").findFirst().orElse(null);
    linkToRecord(obj.addComplexIdentifier(regNum, "Numéro d'inventaire:"));
    obj.addTitle(s.getMulti("title").findFirst().orElse(null));

    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .forEach(image -> {
              image.setContentUrl("http://silknow.org/silknow/media/les-arts-decoratifs/" + image.getContentUrl().substring(image.getContentUrl().lastIndexOf('/') + 1));
              this.linkToRecord(image);
            });

    Production prod = new Production(id);
    prod.add(obj);

    s.getMulti("Création:").forEach(prod::addTimeAppellation);

    s.getMulti("Textile:").forEach(prod::addMaterial);
    s.getMulti("Création:").forEach(prod::addPlace);
    s.getMulti("Matières et techniques:").forEach(prod::addTechnique);
    s.getMulti("Domaine")
            .map(x -> obj.addClassification(x, "Domaine"))
            .forEach(this::linkToRecord);
    s.getMulti("Appellation")
            .map(x -> obj.addClassification(x, "Appellation"))
            .forEach(this::linkToRecord);



    String dim = s.getMulti("Mesures:").findFirst().orElse(null);
    if (dim != null) {
      dim = dim.replace('\n', ' ');
      dim = dim.replace("(en cm)", "en cm");
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.getMulti("description").findFirst().orElse(null), "en", "description"));
    //linkToRecord(obj.addObservation(s.get("TECHNICAL DESCRIPTION"), mainLang, "technical description"));

    String acquisitionFrom = s.getMulti("Credit Line:").findFirst().orElse(null);
    String acquisitionType = s.getMulti("Acquisition/dépôt:").findFirst().orElse(null);
    LegalBody museum = null;


    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setType(acquisitionType);


    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    Collection collection = new Collection(id);
    collection.of(obj);
    collection.addAppellation(s.getMulti("Département").findFirst().orElse(null));


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
