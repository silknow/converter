package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.geonames.Toponym;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.commons.GeoNames;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
      throw new RuntimeException("MAD converter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "mad";

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


    String regNum = s.getMulti("Numéro d'inventaire:").findFirst().orElse(null);
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Numéro d'inventaire:"));
    //obj.addTitle(s.getMulti("title").findFirst().orElse(null));

    List<String> creation_notes = s.getMulti("Création:").map(Object::toString).collect(Collectors.toList());

    final List<String> terms = new ArrayList<>();
    terms.add((s.getMulti("Textile:").findFirst().orElse(null)));
    terms.add(String.join(", ", creation_notes));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);


    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("les-arts-decoratifs"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.addNote(String.join(", ", creation_notes));
    prod.add(obj);

    for (String x : creation_notes) { // Sonia Delaunay, Paris, 1927
      if (StringUtils.isBlank(x)) continue;
      x = x.replace("?", "").trim();

      if (x.contains("siècle") || x.matches("^.*(\\d{4}).*$")) { // it is a date
        if (x.contains("(")) {
          String[] parts = x.split("[()]");
          x = parts[1] + " " + parts[0];
          x = x.trim();
        }
        prod.addTimeAppellation(x);
        continue;
      }
      if (!prod.hasTimeSpans() && x.matches("Louis [XVI]+ \\(époq\\.\\)")) {
        prod.addTimeAppellation(x);
        continue;
      }

      Toponym location = GeoNames.query(x);
      if (location != null) { // it is a place
        prod.addPlace(new Place(location));
      }
    }

    s.getMulti("Matières et techniques:").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Domaine")
      .forEach(x -> linkToRecord(obj.addClassification(x, "Domaine", "fr")));
    s.getMulti("Textile:")
      .forEach(x -> linkToRecord(obj.addClassification(x, "Textile", "fr")));

    s.getMulti("Appellation")
      .map(x -> obj.addClassification(x, "Appellation", mainLang))
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


    String acquisitionType = s.getMulti("Acquisition/dépôt:").findFirst().orElse(null);
    LegalBody museum = null;

    Acquisition acquisition = new Acquisition(regNum);
    //acquisition.transfer(acquisitionFrom, obj, museum);
    //acquisition.setType(acquisitionType);

    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);

    String appellation = s.getMulti("field-skpublishedin").findFirst().orElse(null);
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
