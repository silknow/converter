package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StEtienneConverter extends Converter {

  private static final String DIMENSION_REGEX = "hauteur hors tout en cm : (\\d+(,\\d+)*(\\.\\d*)?) -- largeur en cm : (\\d+(,\\d+)*(\\.\\d*)?) --";
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
    this.DATASET_NAME = "musee-st-etienne";

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


    String regNum = s.getId();
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);

    if (s.getUrl() != null) {
      linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));
    }


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
        });
    }


    if ((s.getMulti("Fonction / Rôle") != null) && (s.get("Personne") != null)) {

      String functions = (s.getMulti("Fonction / Rôle")).collect(Collectors.joining(", "));

    Actor personne = new Actor(s.get("Personne"));
    prod.addActivity(personne, functions);
  }


    s.getMulti("Epoque, datation").forEach(prod::addTimeAppellation);

    if (s.getMulti("Notes") != null) {
    s.getMulti("Notes")
      .map(x -> obj.addObservation(x, "Notes", mainLang))
      .forEach(this::linkToRecord); }

    if (s.getMulti("Observations") != null) {
      s.getMulti("Observations")
        .map(x -> obj.addObservation(x, "Observation", mainLang))
        .forEach(this::linkToRecord); }

    if (s.getMulti("Description analytique") != null) {

      s.getMulti("Description analytique")
        .map(x -> obj.addObservation(x, "Description analytique", mainLang))
        .forEach(this::linkToRecord);
    }


    String dim = (s.getMulti("Mesures")).collect(Collectors.joining(" -- "));
    if (dim != null) {
      dim = dim.replace("(","").replace(")","");
      //System.out.println(dim);
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      //System.out.println(matcher);
      if (matcher.find()) {
        //System.out.println(matcher.group(4));
        //System.out.println(matcher.group(1));
        linkToRecord(obj.addMeasure(matcher.group(4), matcher.group(1)));
      }
    }

    s.getMulti("Sujet / thème").forEach(subject -> obj.addSubject(subject, mainLang));

    String museumName = "Musée d'Art et d'Industrie";
    LegalBody museum = new LegalBody(museumName);

    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(null, obj, museum);

    String sj = s.getMulti("Nom du catalogueur").findFirst().orElse(null);
    if (sj != null) {
      Right right = new Right(obj.getUri() + "/right");
      right.addNote(sj, mainLang);
      right.applyTo(obj);
      linkToRecord(right);
    }

    s.getMulti("Objet géré")
      .map(x -> new PropositionalObject(x))
      .peek(x -> x.isAbout(obj))
      .peek(x -> x.refersTo(model.createResource("http://data.silknow.org/object/"+ ConstructURI.generateUUID((x.getId().trim().split("\\s+")[0] + ".json" + "$$$" + "null" + "http://data.silknow.org/musee-st-etienne")))))
      .forEach(this::linkToRecord);



    if (s.getMulti("Intégrité").findAny() != null) {
      s.getMulti("Intégrité")
        .forEach(cdt -> {
      ConditionAssessment conditionAssessment = new ConditionAssessment(regNum);
      conditionAssessment.concerns(obj);
      conditionAssessment.addCondition("Intégrité", cdt, mainLang);
      linkToRecord(conditionAssessment);
    });}

    if (s.getMulti("Z1C1").findAny() != null) {

      String finalRegNum = regNum;
      InformationObject bio = new InformationObject(finalRegNum + "_b_" + "Z1C1");
      bio.addInformationObjectType("Bibliography", mainLang);
      bio.isAbout(obj);

      s.getMulti("Z1C1")
        .forEach(x -> {

          bio.addNote(x, mainLang);
          linkToRecord(bio);
        });}

    if (s.getMulti("Z1C2").findAny() != null) {

      String finalRegNum = regNum;
      InformationObject bio = new InformationObject(finalRegNum + "_b_" + "Z1C2");
      bio.addInformationObjectType("Bibliography", mainLang);
      bio.isAbout(obj);

      s.getMulti("Z1C2")
        .forEach(x -> {

          bio.addNote(x, mainLang);
          linkToRecord(bio);
        });}

    if (s.getMulti("Z2C1").findAny() != null) {

      String finalRegNum = regNum;
      InformationObject bio = new InformationObject(finalRegNum + "_b_" + "Z2C1");
      bio.addInformationObjectType("Bibliography", mainLang);
      bio.isAbout(obj);

      s.getMulti("Z2C1")
        .forEach(x -> {

          bio.addNote(x, mainLang);
          linkToRecord(bio);
        });}

    if (s.getMulti("Z3C1").findAny() != null) {

      String finalRegNum = regNum;
      InformationObject bio = new InformationObject(finalRegNum + "_b_" + "Z3C1");
      bio.addInformationObjectType("Bibliography", mainLang);
      bio.isAbout(obj);

      s.getMulti("Z3C1")
        .forEach(x -> {

          bio.addNote(x, mainLang);
          linkToRecord(bio);
        });}


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
