package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MADConverter extends Converter {

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
      throw new RuntimeException("MADconverter require files in JSON format.");

    //String mainLang = file.getName().replace(".json", "");
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
    s.setMultiSeparator(" -");

    // Create the objects of the graph
    logger.trace("creating objects");

    id = file.getName().replace(".json", "");

    //String museumName = s.get("MUSEUM");

    ManMade_Object obj = new ManMade_Object(id);
    String regNum = s.get("Numéro d'inventaire:");
    linkToRecord(obj.addComplexIdentifier(regNum, "Numéro d'inventaire:"));
    s.getMulti("title").forEach(obj::addTitle);


    Image img = new Image(s.get("image"));
    obj.add(img);


    Production prod = new Production(id);
    prod.add(obj);

    s.getMulti("Création:").forEach(prod::addTimeAppellation);
    s.getMulti("Matières et techniques:").forEach(prod::addMaterial);
    s.getMulti("Création:").forEach(prod::addPlace);
    //s.getMulti("TÈCNICA*").forEach(prod::addTechnique);
    s.getMulti("Domaine")
            .map(x -> obj.addClassification(x, "Domaine"))
            .forEach(this::linkToRecord);
    s.getMulti("Appellation")
            .map(x -> obj.addClassification(x, "Appellation"))
            .forEach(this::linkToRecord);
    //s.getMulti("DECORACIÓ*").forEach(obj::addSubject);
    //s.getMulti("DESTÍ DÚS*").forEach(obj::addIntention);



    String dim = s.get("Mesures");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("description"), "en", "description"));
    //linkToRecord(obj.addObservation(s.get("TECHNICAL DESCRIPTION"), mainLang, "technical description"));

    String acquisitionFrom = s.get("Credit Line:");
    String acquisitionType = s.get("Acquisition/dépôt:");
    //String acquisitionDate = s.get("YEAR ENTERED THE MUSEUM");
    LegalBody museum = null;
    //if (museumName != null)
      //museum = new LegalBody(museumName);

    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(acquisitionFrom, obj, museum);
    //acquisition.setDate(acquisitionDate);
    acquisition.setType(acquisitionType);


    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);


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
