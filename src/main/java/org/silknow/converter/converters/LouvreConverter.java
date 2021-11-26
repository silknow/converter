package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LouvreConverter extends Converter {

  private static final String DIMENSION_REGEX = "Largeur : (\\d+(?:\\.\\d+)?) cm ; Hauteur : (\\d+(?:\\.\\d+)?) cm";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("LouvreConverter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "louvre";


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


    String regNum = s.getMulti("Numéro d’inventaire").findFirst().orElse(null);

    if (regNum == null)
      regNum = s.getId();
    id = s.getId();

    String museumName = "Musée du Louvre";


    ManMade_Object obj = new ManMade_Object(regNum);

    s.getMulti("Type d'objet")
      .map(x -> obj.addClassification(x, "Type d'objet", mainLang))
      .forEach(this::linkToRecord);

    s.getMulti("Catégorie")
      .map(x -> obj.addClassification(x, "Catégorie", mainLang))
      .forEach(this::linkToRecord);

    s.getMulti("Type")
      .map(x -> obj.addClassification(x, "Type", mainLang))
      .forEach(this::linkToRecord);

    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));


    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("louvre"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);


    s.getMulti("Artiste / Auteur / Ecole / Centre artistique").forEach(author -> prod.addActivity(new Actor (author), "Artiste / Auteur / Ecole / Centre artistique"));

    if (s.getMulti("title").findAny() != null) {
      obj.addTitle(s.getMulti("title").findFirst().orElse(null), mainLang);
    }


    if (s.getMulti("Description / Décor").findFirst().orElse(null) != null) {
      s.getMulti("Description / Décor")
        .map(x -> obj.addObservation(x, "Description / Décor", mainLang))
        .forEach(this::linkToRecord);
    }



    String dim = s.getMulti("Dimensions").findFirst().orElse(null);
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }

    s.getMulti("Date de création / fabrication").forEach(time -> prod.addTimeAppellation(time.replaceAll("Epoque / période : ","").replaceAll("Date de création/fabrication : ","")));

    s.getMulti("Matière et technique").forEach(material -> prod.addMaterial(material.replaceAll("Matériau/Technique : ","").replaceAll("Matériau : ","").replaceAll("Technique :",""), mainLang));
    s.getMulti("Lieu de création / fabrication / exécution").forEach(prod::addPlace);




    if (s.getMulti("Collection").findFirst().orElse(null) != null) {
      Collection collection = new Collection(regNum, s.getMulti("Collection").findFirst().orElse(null));
      collection.of(obj);
      collection.addAppellation(s.getMulti("Collection").findFirst().orElse(null));
      linkToRecord(collection);
    }

    if (s.getMulti("Mode d’acquisition").findFirst().orElse(null) != null){

      String acquisitionFrom = s.getMulti("Mode d’acquisition").findFirst().orElse(null);
      LegalBody museum = new LegalBody(museumName);

      Acquisition acquisition = new Acquisition(regNum);
      acquisition.transfer(acquisitionFrom, obj, museum);
      acquisition.addActor(new Actor(acquisitionFrom));
    }


    AtomicInteger Pcounter = new AtomicInteger();


    if (s.getMulti("Bibliographie").findAny() != null) {

      String finalRegNum1 = regNum;
      s.getMulti("Bibliographie")
        .forEach(x -> {

          InformationObject pub = new InformationObject(finalRegNum1 + "_b_" + Pcounter.getAndIncrement());
          pub.addInformationObjectType(s.get("Bibliographie"), mainLang);
          pub.isAbout(obj);
          pub.addNote(x, mainLang);
          linkToRecord(pub);
        });}

    AtomicInteger Ecounter = new AtomicInteger();

    if (s.getMulti("Expositions").findAny() != null) {

      String finalRegNum = regNum;
      s.getMulti("Expositions")
        .forEach(y -> {

          InformationObject exh = new InformationObject(finalRegNum + "_e_" + Ecounter.getAndIncrement());
          exh.setType("Expositions", mainLang);
          exh.isAbout(obj);
          exh.addNote(y, mainLang);
          linkToRecord(exh);
        });}

    linkToRecord(obj);

    linkToRecord(prod);
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
