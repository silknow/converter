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

    String mainLang = "en";
    this.DATASET_NAME = "mfa";

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
    String museumName = "MFA Boston";

    String regNum = s.get("Accession Number");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    //s.getMulti("titleField").forEach(obj::addTitle);
    s.getMulti("titleField")
            .map(x -> obj.addClassification(x, "titleField", mainLang))
            .forEach(this::linkToRecord);
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));

    /*
    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("titleField").findFirst().orElse(null)));
    terms.add((s.getMulti("displayDateField").findFirst().orElse(null)));
    terms.add((s.getMulti("cultureField").findFirst().orElse(null)));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);
/*


     */
    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("mfa-boston"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("displayDateField").forEach(prod::addTimeAppellation);
    s.getMulti("cultureField").forEach(prod::addPlace);


    //String[] teas = s.get("teaser").split("(?=[0-9])", 2);
    //if (teas.length > 1) {
    //  prod.addPlace(teas[0]);
    //  prod.addTimeAppellation(teas[1]);
    //}

    s.getMulti("Medium/Technique").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Classifications")
            .map(x -> obj.addClassification(x, "Classifications", mainLang))
            .forEach(this::linkToRecord);

    String dim = s.get("Dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("Description"), "Description", mainLang));



    LegalBody museum = null; // FIXME?



    if (s.get("Provenance") != null) {
      Acquisition acquisition = new Acquisition(regNum);
      String acquisitionFrom = s.get("Provenance") + " " + s.get("Credit Line");
      acquisition.transfer(acquisitionFrom, obj, museum);
      acquisition.addActor(new Actor(acquisitionFrom));
      linkToRecord(acquisition);
    }


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);

    Collection collection = new Collection(regNum, s.getMulti("Collections").findFirst().orElse(null));
    collection.of(obj);
    collection.addAppellation(s.getMulti("Collections").findFirst().orElse(null));

    linkToRecord(collection);
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
