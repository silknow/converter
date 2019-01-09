package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.commons.Utils;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JocondeConverter extends Converter {
  private final String DATASET_NAME = "joconde";

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
      throw new RuntimeException("Imatex converter require files in JSON format.");

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
    id = file.getName().replace(".json", "");

    String museumName = s.get("Lieu de conservation");
    LegalBody museum = new LegalBody(museumName);

    Document doc = new Document(id);
    s.getMulti("Rédacteur").map(Person::new)
            .forEach(doc::addEditor);

    ManMade_Object obj = new ManMade_Object(id);
//    obj.addComplexIdentifier(id, "Register number", museum, doc);
    obj.addTitle(s.get("Titre"));
    obj.addClassification(s.get("Domaine"), "domain");
    obj.addClassification(s.get("Dénomination"), "denomination");
    obj.addNote(s.get("Description"));
    obj.addSubject(s.get("Sujet représenté"));

    String dim = s.get("Dimensions");
    Matcher matcher = DIMENSION_PATTERN.matcher(dim);
    if (matcher.find()) {
      obj.addMeasure(matcher.group(2), matcher.group(1));
    }

    Production prod = new Production(id);
    prod.add(obj);
    s.getMulti("Période création/exécution").forEach(prod::addTimeAppellation);

    String author = s.get("Auteur/exécutant");
    List<String> parts = Utils.extractBrackets(author);
    author = parts.get(0);
    String role = parts.get(1);
    Actor actor = new Actor(author);
    actor.addNote(s.get("Précision auteur/exécutant"));
    prod.addActivity(actor, role);

    // TODO decide what is material and what technique
    Arrays.asList(s.get("Matériaux/techniques").split(", ?"))
            .forEach(prod::addMaterial);

    String place = s.get("Lieu création / utilisation");
    if (place != null) {
      place = Utils.extractBrackets(place).get(0);
      String[] hierarchy = place.split(",");
//      String country = hierarchy[0];
      String city = hierarchy[hierarchy.length - 1];

      Place pl = new Place(city);
      prod.addPlace(pl);
    }

    String hist = null;
    for (String h : s.getMulti("Historique").collect(Collectors.toList())) {
      if (h.startsWith("voir aussi :")) hist = h;
      else obj.addNote(h.replaceAll("(^\\(|\\)$)", ""));
    }

    PropositionalObject po = new PropositionalObject(id);
    po.isAbout(obj);
    po.addNote(s.get("Précision sujet représenté"), "fr");

    String gen = s.get("Genèse");
    if ("objet en rapport".equalsIgnoreCase(gen)) {
      po.setType(gen);

      assert hist != null;
      while (Utils.containsBrackets(hist)) {
        List<String> temp = Utils.extractBrackets(hist);
        hist = temp.get(0);
        String id_ = temp.get(1);
        ManMade_Object o = new ManMade_Object(id_);
        po.refersTo(o);
        po.addNote(hist);
      }
    }

    List<String> inscr = s.getMulti("Inscriptions").collect(Collectors.toList());
    List<String> inscrPrec = s.getMulti("Précision inscriptions").collect(Collectors.toList());

    for (int i = 0; i < inscrPrec.size(); i++) {
      String p = inscrPrec.get(i);
      String lang = null;
      if (inscr.size() > i) {
        lang = french2lang(Utils.extractBrackets(inscr.get(i)).get(1));
      }

      Inscription ins = Inscription.fromJoconde(p, lang);
      obj.add(ins);
    }

    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);

    String sj = s.get("Statut juridique");
    Right right = new Right(obj.getUri() + "/right");
    if (sj.contains(museumName)) right.ownedBy(museum);
    right.addNote(sj, "fr");
    right.applyTo(obj);

    PropositionalObject record = new PropositionalObject(id + "r");
    record.setType("museum record");
    record.isAbout(obj);
    Right copyright = new Right(record.getUri() + "/right");
    copyright.applyTo(record);
    copyright.addNote(s.get("Copyright notice"));
    s.getMulti("Copyright notice", ", ")
            .map(x -> x.replaceFirst("© ", ""))
            .map(LegalBody::new)
            .forEach(copyright::ownedBy);


    Map<String, String> ids = new HashMap<>();
    s.getMulti("Numéro d'inventaire")
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

    obj.addComplexIdentifier(ids.remove("Register number"), "Register number", JOCONDE, doc, oldId);
    ids.keySet().forEach(x -> obj.addComplexIdentifier(ids.get(x), x, JOCONDE, doc));

    Right copyphoto = new Right(obj.getUri() + "/image/right");
    copyphoto.addNote(s.get("Crédits photographiques"));
    s.getMulti("Copyright notice", ", ")
            .map(x -> x.replaceFirst("© ", ""))
            .map(Actor::new)
            .forEach(copyright::ownedBy);
    s.getImages().map(Image::fromCrawledJSON)
            .peek(copyphoto::applyTo)
            .forEach(obj::add);


    if (s.get("Bibliographie") != null) {
      InformationObject bio = new InformationObject(id + "b");
      bio.setType("Bibliography");
      bio.isAbout(obj);
      bio.addNote(s.get("Bibliographie"));
      obj.getModel().add(bio.getModel());
    }
    if (s.get("Exposition") != null) {
      InformationObject bio = new InformationObject(id + "e");
      bio.setType("Exhibitions");
      bio.isAbout(obj);
      bio.addNote(s.get("Exposition"));
      obj.getModel().add(bio.getModel());
    }

    linkToRecord(obj);
    linkToRecord(doc);
    linkToRecord(po);
    linkToRecord(right);
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
