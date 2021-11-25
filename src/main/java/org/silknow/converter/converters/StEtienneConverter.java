package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
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
import java.util.stream.Stream;

public class StEtienneConverter extends Converter {

  private static final String DIMENSION_REGEX = "hauteur (en cm) : (\\d+?), largeur (en cm) : (\\d+?) ";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);
  private static final String AUTHOR_ROLE_REGEX = "Author: (.+) - Role: (.+)";
  private static final Pattern AUTHOR_ROLE_PATTERN = Pattern.compile(AUTHOR_ROLE_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("StEtienne converter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "stetienne";

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


    String regNum = s.getMulti("Numéro d'inventaire").findFirst().orElse(null);
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));


    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("Désignation du bien").findFirst().orElse(null)));
    terms.add((s.getMulti("Epoque, datation").findFirst().orElse(null)));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);

    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("st-etienne"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    Production prod = new Production(regNum);

    prod.add(obj);



    s.getMulti("Matière").forEach(material -> prod.addMaterial(material, mainLang));

    s.getMulti("Technique").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("Matière").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Lieu").forEach(prod::addPlace);


    s.getMulti("Désignation du bien")
      .forEach(x -> linkToRecord(obj.addClassification(x, "Désignation du bien", "fr")));
    s.getMulti("Domaine")
      .forEach(x -> linkToRecord(obj.addClassification(x, "Domaine", "fr")));


    s.getMulti("Transcription")
      .forEach(x -> linkToRecord(obj.addClassification(x, "Désignation du bien", "fr")));

    if (s.getMulti("Transcription").findAny() != null) {
    s.getMulti("Transcription")
      .forEach(x -> {

        Inscription ins = new Inscription(x, mainLang);
        s.getMulti("Type d'inscription").forEach(y -> ins.addNote(y, mainLang));
        s.getMulti("Description").forEach(z -> ins.addNote(z, mainLang));
        s.getMulti("Emplacement").forEach(e -> ins.addNote(e, mainLang));

        linkToRecord(ins);
      });}


    if ((s.get("Fonction / Rôle") != "") && (s.get("Personne") != "")) {
    s.getMulti("Personne").forEach(author -> prod.addActivity(new Actor(author), s.get("Fonction / Rôle")));}


    s.getMulti("Epoque, datation").forEach(prod::addTimeAppellation);

    s.getMulti("Notes")
      .map(x -> obj.addObservation(x, "Notes", mainLang))
      .forEach(this::linkToRecord);

    s.getMulti("Description analytique")
      .map(x -> obj.addObservation(x, "Description analytique", mainLang))
      .forEach(this::linkToRecord);

    s.getMulti("Description analytique")
      .map(x -> obj.addObservation(x, "Description analytique", mainLang))
      .forEach(this::linkToRecord);


    String dim = (s.getMulti("Mesures")).collect(Collectors.joining(","));
    if (dim != null) {
      dim = dim.replace("longueur", "largeur");
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }

    s.getMulti("Sujet / thème").forEach(subject -> obj.addSubject(subject, mainLang));



    Acquisition acquisition = new Acquisition(regNum);




    linkToRecord(obj);
    linkToRecord(acquisition);
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
