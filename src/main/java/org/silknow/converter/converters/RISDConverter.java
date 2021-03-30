package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;
import org.silknow.converter.ontologies.CIDOC;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RISDConverter extends Converter {

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
      throw new RuntimeException("RISD converter require files in JSON format.");

    String mainLang = "en";
    this.DATASET_NAME = "risd";

    // Parse JSON
    logger.trace("parsing json");
    CrawledJSON s;
    try {
      s = CrawledJSON.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    //s.setMultiSeparator(" ///");

    // Create the objects of the graph
    logger.trace("creating objects");

    filename = file.getName();

    //String museumName = s.get("MUSEUM");

    String regNum = s.get("Object Number");
    if (regNum != null)
      id = regNum;
    if (regNum == null)
      id = filename+"_local_filename_id";


    ManMade_Object obj = new ManMade_Object(id);
    linkToRecord(obj.addComplexIdentifier(id, "Object Number"));
    //obj.addTitle(s.getMulti("Title").findFirst().orElse(null));
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));

    /*
    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("Title").findFirst().orElse(null)));
    terms.add((s.getMulti("Year").findFirst().orElse(null)));
    terms.add((s.getMulti("Culture").findFirst().orElse(null)));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);
*/







    s.getMulti("Title")
            .map(x -> obj.addClassification(x, "Title", mainLang))
            .forEach(this::linkToRecord);


    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("risd-museum"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(id);
    prod.add(obj);
    prod.addActivity(s.getMulti("Maker").findFirst().orElse(null), "Maker");

    s.getMulti("Year").forEach(prod::addTimeAppellation);
    s.getMulti("Materials").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Medium").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Culture").forEach(prod::addPlace);
    s.getMulti("Techniques").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("Type")
            .map(x -> obj.addClassification(x, "Type", mainLang))
            .forEach(this::linkToRecord);


    String dim = s.get("Dimensions");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("description"), "Description", mainLang));

    String acquisitionFrom = s.get("Credit");
    LegalBody museum = null;

    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(acquisitionFrom, obj, museum);

    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    s.getExhibitions().map(InformationObject::fromCrawledJSON)
            .forEach(pub -> {
              pub.addProperty(CIDOC.P129_is_about, obj);
              this.linkToRecord(pub);
            });

    s.getPublications().map(InformationObject::fromCrawledJSON)
            .forEach(pub -> {
              pub.addProperty(CIDOC.P129_is_about, obj);
              this.linkToRecord(pub);
            });

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
