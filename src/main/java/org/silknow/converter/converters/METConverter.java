package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.ConstructURI;
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
    id = s.getId();

    ManMade_Object obj = new ManMade_Object(regNum);
    //obj.addTitle(s.getMulti("title").findFirst().orElse(null));
    s.getMulti("title")
            .map(x -> obj.addClassification(x, "Title", mainLang))
            .forEach(this::linkToRecord);

    s.getMulti("relatedObjects")
      .map(x -> new PropositionalObject(x))
      .peek(x -> x.isAbout(obj))
      .peek(x -> x.refersTo(model.createResource("http://data.silknow.org/object/"+ ConstructURI.generateUUID((x.getId() + ".json" + "$$$" + x.getId() + "http://data.silknow.org/met")))))
      .forEach(this::linkToRecord);



/*
    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("title").findFirst().orElse(null)));
    terms.add((s.getMulti("Date:").findFirst().orElse(null)));
    terms.add((s.getMulti("Culture:").findFirst().orElse(null)));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);
*/



    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("Date:").forEach(prod::addTimeAppellation);
    s.getMulti("Medium:").forEach(material -> prod.addMaterial(material, mainLang));

    s.getMulti("Object Type / Material").forEach(material -> prod.addMaterial(material.replaceAll(" *\\(.+?\\)", ""), mainLang));

    //s.getMulti("Object Type / Material").forEach(x -> obj.addClassification(x.replaceAll(" *\\(.+?\\)", ""), "Object Type / Material",  mainLang));
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));




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


    s.getMulti("Classification:").forEach(x -> obj.addClassification(x.replaceAll(" *\\(.+?\\)", ""), "Classification",  mainLang));


    String dim = s.get("Dimensions:");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }


    linkToRecord(obj.addObservation(s.get("description"), "Description", "en"));

    String acquisitionFrom = s.get("Credit Line:");
    LegalBody museum = null; // FIXME ?


    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.addActor(new Actor(acquisitionFrom));





    Right copyphoto = new Right(obj.getUri() + "/image/right/");
    copyphoto.addNote(s.get("imagesRightsLink"));
    s.getMulti("imagesRightsText", ", ")
      .map(x -> x.replaceFirst("© ", ""))
      .map(Actor::new)
      .forEach(copyphoto::ownedBy);

    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("met-museum"))
      .peek(obj::add)
      .peek(copyphoto::applyTo)
      .forEach(this::linkToRecord);


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
