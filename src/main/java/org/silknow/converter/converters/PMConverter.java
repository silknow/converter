package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.List;
import java.util.Objects;

public class PMConverter extends Converter {


  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("PMConverter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "PM";

    // Parse JSON
    logger.trace("parsing json");
    PMRecord s;
    try {
      s = PMRecord.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    //s.setMultiSeparator(" -");

    // Create the objects of the graph
    logger.trace("creating objects");

    filename = file.getName();
    String museumName = "Paris Musées";

    String regNum = s.get("fieldOeuvreNumInventaire");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    obj.addTitle(s.getMulti("title").findFirst().orElse(null), mainLang);
    //obj.addTitle(s.getMulti("Autre Titre").findFirst().orElse(null),mainLang);
    s.getMulti("fieldOeuvreTypesObjet")
      .map(x -> obj.addClassification(x, "Type(s) d'objet(s)", mainLang))
      .forEach(this::linkToRecord);
    if (s.getUrl() != null) {
      linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl()))); }
    s.getMulti("fieldDenominations")
      .map(x -> obj.addClassification(x, "Dénomination(s)", mainLang))
      .forEach(this::linkToRecord);

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
     */

    s.getImages().map(Image::fromCrawledJSON)
      .peek(image -> image.addInternalUrl("ParisMusees"))
      .peek(obj::add)
      .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);


    // Authors
    if (s.getAuthors() != null)
    {
      s.getAuthors()
        .filter(Objects::nonNull)
        .map(this::toPerson)
        .forEach(x -> prod.addActivity(x, "author"));

    }

    // Dates
    String startYear = s.get("fieldDateProduction.startYear");
    String endYear = s.get("fieldDateProduction.startYear");
    String century = s.get("fieldDateProduction.century");
    if (startYear != null || endYear != null) {
      prod.addTimeSpan(new TimeSpan(startYear, endYear));
    } else if (century != null) { // this normally do not happen
      prod.addTimeSpan(new TimeSpan(century));
    }

    s.getMulti("fieldOeuvreLieuxProductions").forEach(prod::addPlace);

    s.getMulti("fieldSujetsConcernes").forEach(subject -> obj.addSubject(subject, mainLang));
    s.getMulti("Précision sujet représenté").forEach(subject -> obj.addSubject(subject, mainLang));

    s.getMulti("fieldMateriauxTechnique").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Classifications")
      .map(x -> obj.addClassification(x, "Classifications", mainLang))
      .forEach(this::linkToRecord);

    linkToRecord(obj.addMeasure(s.getMulti("Largeur.Œuvre").findFirst().orElse(""), s.getMulti("Hauteur.Œuvre").findFirst().orElse("")));

    linkToRecord(obj.addObservation(s.get("fieldOeuvreDescriptionIcono.value"), "Description iconographique", mainLang));

    LegalBody legalbody = new LegalBody(s.get("fieldMusee.entity.entityLabel"));

    if (s.get("Nom du donateur, testateur, vendeur") != null) {
      Acquisition acquisition = new Acquisition(regNum);
      String acquisitionFrom = s.get("Nom du donateur, testateur, vendeur");
      acquisition.addActor(new Actor(acquisitionFrom));
      acquisition.transfer(acquisitionFrom, obj, legalbody);
      linkToRecord(acquisition);
    }

    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(legalbody);

    if (s.get("Expositions") != null) {
      InformationObject bio = new InformationObject(regNum + "e");
      bio.setType("Expositions", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("Expositions"), mainLang);
      linkToRecord(bio);
    }

    if (s.get("Documentation") != null) {
      InformationObject bio = new InformationObject(regNum + "e");
      bio.setType("Documentation", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("Documentation"), mainLang);
      linkToRecord(bio);
    }

    Right copyphoto = new Right(obj.getUri() + "/image/right");
    copyphoto.addNote(s.get("(Droits sur l’image)"));
    s.getMulti("(Droits sur l’image)", ", ")
      .map(x -> x.replaceFirst("© ", ""))
      .map(Actor::new)
      .forEach(copyphoto::ownedBy);

    linkToRecord(obj);

    linkToRecord(prod);
    linkToRecord(transfer);
    return this.model;
  }

  private Person toPerson(List<String> author) {
    if (author.size() != 3) return null;
    String name = author.get(0);
    String birth = author.get(1);
    String death = author.get(2);
    return new Person(name, birth, death);
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
