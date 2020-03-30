package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.commons.Utils;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JocondeConverter extends Converter {
  private static final LegalBody JOCONDE = new LegalBody("Joconde");

  private static final String DIMENSION_REGEX = "hauteur en cm (\\d+(?:\\.\\d+)?) ; largeur en cm (\\d+(?:\\.\\d+)?)";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }

  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("Joconde converter require files in JSON format.");

    String mainLang = "fr";
    this.DATASET_NAME = "joconde";

    // Parse JSON
    logger.trace("parsing json");
    CrawledJSON s;
    try {
      s = CrawledJSON.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    s.setMultiSeparator(" ; ");

    // Create the objects of the graph
    logger.trace("creating objects");
    filename = file.getName();

    String regNum = s.get("INV").split(" ")[0];
    id = regNum;

    String museumName = s.get("LOCA");
    LegalBody museum = new LegalBody(museumName);


    Document doc = new Document(id);
    s.getMulti("REDA").map(Person::new)
            .forEach(doc::addEditor);

    ManMade_Object obj = new ManMade_Object(id);
    obj.addTitle(s.get("Titre"));
    s.getMulti("DOMN")
            .forEach(x -> linkToRecord(obj.addClassification(x, "Domaine", "fr")));
    s.getMulti("DENO")
            .forEach(x -> linkToRecord(obj.addClassification(x, "Dénomination", "fr")));
    linkToRecord(obj.addObservation(s.get("DESC"), "Description", mainLang));
    s.getMulti("REPR").forEach(subject -> obj.addSubject(subject, mainLang));


    doc.document(obj);

    String dim = s.get("DIMS");
    Matcher matcher = DIMENSION_PATTERN.matcher(dim);
    if (matcher.find()) {
      linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
    }

    Production prod = new Production(id);
    prod.add(obj);
    s.getMulti("PERI").forEach(prod::addTimeAppellation);

    String author = s.getMulti("AUTR").findFirst().orElse(null);
    List<String> parts = Utils.extractBrackets(author);
    author = parts.get(0);
    String role = parts.get(1);
    Actor actor = new Actor(author);
    actor.addNote(s.get("PAUT"));
    prod.addActivity(actor, role);

    // TODO decide what is material and what technique
    // TODO needs to be rewritten to split up values
    String[] arr = s.getMulti("TECH").toArray(String[]::new);
    for (String a1 : arr) {
      for (String a2 : a1.split(",")) {
        prod.addMaterial(a2, mainLang);
      }
    }


    String place = s.getMulti("LIEUX").findFirst().orElse(null);
    if (place != null) {
      place = Utils.extractBrackets(place).get(0);
      String[] hierarchy = place.split(",");
//      String country = hierarchy[0];
      String city = hierarchy[hierarchy.length - 1];
      prod.addPlace(city);
    }

    String hist = null;
    for (String h : s.getMulti("HIST").collect(Collectors.toList())) {
      if (h.startsWith("voir aussi :")) hist = h;
      else obj.addNote(h.replaceAll("(^\\(|\\)$)", ""));
    }

    PropositionalObject po = new PropositionalObject(id);
    po.isAbout(obj);
    po.addNote(s.getMulti("PREP").findFirst().orElse(null), mainLang);

    //String gen = s.getMulti("GENE").findFirst().orElse(null);
    //if ("objet en rapport".equalsIgnoreCase(gen)) {
      //po.setType(gen, mainLang);

      //assert hist != null;
      //po.addNote(hist);
      //while (Utils.containsBrackets(hist)) {
        //List<String> temp = Utils.extractBrackets(hist);
        //hist = temp.get(0);
        //String id_ = temp.get(1);
        //ManMade_Object o = new ManMade_Object(id_);
        //po.refersTo(o);
      //}
    //}

    List<String> inscr = s.getMulti("INSC").collect(Collectors.toList());
    List<String> inscrPrec = s.getMulti("Précision inscriptions").collect(Collectors.toList());

    for (int i = 0; i < inscrPrec.size(); i++) {
      String p = inscrPrec.get(i);
      String lang = null;
      String type = null;
      String note = null;
      if (inscr.size() > i) {
        note = inscr.get(i);
        List<String> temp = Utils.extractBrackets(note);
        lang = french2lang(temp.get(1));
        type = temp.get(0);
      }

      Inscription ins = Inscription.fromJoconde(p, lang);
      if (type != null) {
        linkToRecord(ins.addClassification(type, null, mainLang));
      }
      if (note != null) {
        linkToRecord(ins.addObservation(note, "Inscription", "fr"));
      }
      obj.add(ins);
    }

    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    String sj = s.getMulti("STAT").findFirst().orElse(null);
    if (sj != null) {
      Right right = new Right(obj.getUri() + "/right");
      if (sj.contains(museumName)) right.ownedBy(museum);
      right.addNote(sj, mainLang);
      right.applyTo(obj);
      linkToRecord(right);
    }

    PropositionalObject record = new PropositionalObject(id + "r");
    record.setType("museum record", mainLang);
    record.isAbout(obj);
    Right copyright = new Right(record.getUri() + "/right");
    copyright.applyTo(record);
    copyright.addNote(s.get("COPY"));
    s.getMulti("COPY", ", ")
            .map(x -> x.replaceFirst("© ", ""))
            .map(LegalBody::new)
            .forEach(copyright::ownedBy);


    Map<String, String> ids = new HashMap<>();
    s.getMulti("INV")
            .map(Utils::extractBrackets)
            .map(ArrayList::new)
            .peek(x -> {
              if (x.get(1) == null) {
                x.remove(1);
                x.add("Register number");
              }
            })
            .forEach(x -> ids.put(x.get(1), x.get(0)));

    String oldId = ids.remove("ancien numéro");

    linkToRecord(obj.addComplexIdentifier(ids.remove("Register number"), "Register number", JOCONDE, oldId));
    ids.keySet().forEach(x -> linkToRecord(obj.addComplexIdentifier(ids.get(x), x, JOCONDE)));

    Right copyphoto = new Right(obj.getUri() + "/image/right");
    copyphoto.addNote(s.get("PHOT"));
    s.getMulti("PHOT", ", ")
            .map(x -> x.replaceFirst("© ", ""))
            .map(Actor::new)
            .forEach(copyphoto::ownedBy);


    s.getImages().map(Image::fromCrawledJSON)
            .peek(copyphoto::applyTo)
            .peek(image -> image.addInternalUrl("joconde"))
            .peek(obj::add)
            .forEach(this::linkToRecord);


    if (s.get("BIBL") != null) {
      InformationObject bio = new InformationObject(id + "b");
      bio.setType("Bibliographie", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("BIBL"));
      linkToRecord(bio);
    }
    if (s.get("EXPO") != null) {
      InformationObject bio = new InformationObject(id + "e");
      bio.setType("Exposition", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("EXPO"));
      linkToRecord(bio);
    }

    linkToRecord(obj);
    linkToRecord(doc);
    linkToRecord(po);
    linkToRecord(copyright);
    linkToRecord(prod);
    linkToRecord(transfer);
    return this.model;
  }

  @Contract("null -> null")
  @Nullable
  private String french2lang(String txt) {
    if (txt == null) return null;
    switch (txt) {
      case "anglais":
        return "en";
      case "français":
        return "fr";
      case "italien":
        return "it";
      default:
        logger.warn("Not recognised language: " + txt);
        return null;
    }
  }

}
