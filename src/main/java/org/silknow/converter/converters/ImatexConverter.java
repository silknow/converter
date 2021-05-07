package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImatexConverter extends Converter {
//  private static final String DOC_BASE_URI = "http://imatex.cdmt.cat/_cat/fitxa_fitxa.aspx?num_id=";

  private static final String MOD_REGEX = "(.+)(?: -|,|.) (\\d{4})(?: \\((.+)\\))?";
  private static final Pattern MOD_PATTERN = Pattern.compile(MOD_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("Imatex converter require files in JSON format.");

    String mainLang = file.getName().replace(".json", "").split("-")[1];
    this.DATASET_NAME = "imatex";

    // Parse JSON
    logger.trace("parsing json");
    CrawledJSON s;
    try {
      s = CrawledJSON.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    s.setMultiSeparator(" -");

    // Create the objects of the graph
    logger.trace("creating objects");

    String fullfilename = file.getName();
    fullfilename = fullfilename.replace("-en", "");
    fullfilename = fullfilename.replace("-es", "");
    fullfilename = fullfilename.replace("-ca", "");

    filename = fullfilename;


    String museumName = s.get("MUSEUM");

    String regNumField = null;
    switch (mainLang) {
      case "en":
        regNumField = "REGISTER NUMBER";
        break;
      case "ca":
        regNumField = "NUM. REGISTRE";
        break;
      case "es":
        regNumField = "NÚM.REGISTRO";
    }

    String regNum = s.get(regNumField);
    if (regNum == null)
      regNum = s.get("REGISTER NUMBER");
    if (regNum == null)
      regNum = s.get("NUM. REGISTRE");
    if (regNum == null)
      regNum = s.get("NÚM.REGISTRO");
    if (regNum == null)
      regNum = s.getId();
     id = s.getId();


    ManMade_Object obj = new ManMade_Object(regNum);



    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("imatex"))
            .peek(obj::add)
            .forEach(this::linkToRecord);


    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("CRONOLOGIA*").forEach(prod::addTimeAppellation);
    s.getMulti("MATÈRIES*").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("ORIGEN*").forEach(prod::addPlace);
    s.getMulti("TÈCNICA*").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("CLASSIFICACIÓ GENÈRICA*")
            .map(x -> obj.addClassification(x, "CLASSIFICACIÓ GENÈRICA", mainLang))
            .forEach(this::linkToRecord);
    s.getMulti("DENOMINACIÓ*")
            .map(x -> obj.addClassification(x, "DENOMINACIÓ", mainLang))
            .forEach(this::linkToRecord);
    s.getMulti("DECORACIÓ*").forEach(subject -> obj.addSubject(subject, mainLang));

    //s.getMulti("DESTÍ DÚS*").forEach(obj::addIntention);
    //s.getMulti("DESTÍ DÚS*")
    //        .map(x -> obj.addClassification(x, "DESTÍ DÚS", mainLang))
    //        .forEach(this::linkToRecord);


    String cdt = s.get("ESTAT DE CONSERVACIÓ*");
    if (cdt != null) {
      ConditionAssessment conditionAssessment = new ConditionAssessment(regNum);
      conditionAssessment.concerns(obj);
      conditionAssessment.addCondition("condition", cdt, mainLang);
      linkToRecord(conditionAssessment);
    }



    String rest = s.get("RESTAURACIÓ*");
    if (rest != null && !rest.equalsIgnoreCase("no")) {
      if (rest.contains("/")) {
        String[] separated = rest.split("/");
        for (String element : separated) {
          Matcher matcher2 = MOD_PATTERN.matcher(element);
          if (matcher2.find()) {
            Modification modification = new Modification(id, matcher2.group(1), matcher2.group(2), matcher2.group(3));
            modification.of(obj);
            linkToRecord(modification);
          }
          else {
            Modification modification = new Modification(id, element);
            modification.of(obj);
            linkToRecord(modification);
          }
        }
      }
      else {
        Matcher matcher2 = MOD_PATTERN.matcher(rest);
        if (matcher2.find()) {
          Modification modification = new Modification(id, matcher2.group(1), matcher2.group(2), matcher2.group(3));
          modification.of(obj);
          linkToRecord(modification);
        }
        else {
          Modification modification = new Modification(id, rest);
          modification.of(obj);
          linkToRecord(modification);
        }
      }
    }





    linkToRecord(obj.addMeasure(s.get("MEASUREMENT")));
    linkToRecord(obj.addObservation(s.get("DESCRIPTION"), "Description", "en"));
    linkToRecord(obj.addObservation(s.get("DESCRIPCIÓN"), "Description", "es"));
    linkToRecord(obj.addObservation(s.get("DESCRIPCIÓ"), "Description", "ca"));
    linkToRecord(obj.addObservation(s.get("DESCRIPCIÓ TÈCNICA\n"), "Technical description", "ca"));
    linkToRecord(obj.addObservation(s.get("DESCRIPCIÓN TÉCNICA"), "Technical description", "es"));
    linkToRecord(obj.addObservation(s.get("TECHNICAL DESCRIPTION"), "Technical description", "en"));

    String acquisitionFrom = s.get("FONT INGRÉS*");
    String acquisitionDate = s.get("YEAR ENTERED THE MUSEUM");
    LegalBody museum = null;
    if (museumName != null)
      museum = new LegalBody(museumName);

    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setDate(acquisitionDate);
    acquisition.addNote(s.get("FORMA INGRÉS*"));


    String npa = s.get("NOMS PROPIS ASSOCIATS*");
    if (npa != null) {
      if (!npa.equals("Homar, Gaspar, 1870-1955 -")) {
        Activity activity = new Activity(regNum, "Noms Propis Associats");
        activity.addActor(npa);
        linkToRecord(activity);
      }
      if (npa.equals("Homar, Gaspar, 1870-1955 -")) {
        prod.addActivity(npa, "Noms Propis Associats");
      }
    }

    prod.addActivity(s.get("DESSIGNER"), "Designer");
    prod.addActivity(s.get("MANUFACTURER"), "Maker");
    // prod.addActivity(s.get("TAILOR/COUTURIER"), "Tailor/Couturier");
    // Pierre explained that this field was not useful; for the period of interest to SILKNOW, there is no tailor/couturier
    prod.addActivity(s.get("AUTHOR"), "Author");

    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);

    if (s.get("BIBLIOGRAPHY") != null) {
      InformationObject bio = new InformationObject(regNum + "b");
      bio.setType("Bibliography", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("BIBLIOGRAPHY"), mainLang);
      linkToRecord(bio);
    }

    if (s.get("EXHIBITIONS") != null) {
      InformationObject bio = new InformationObject(regNum + "e");
      bio.setType("Exhibitions", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("EXHIBITIONS"), mainLang);
      linkToRecord(bio);
    }

    if (s.get("OTHER ITEMS") != null) {
      InformationObject bio = new InformationObject(regNum + "e");
      bio.setType("Other items", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("OTHER ITEMS"), mainLang);
      linkToRecord(bio);
    }

    linkToRecord(obj);
    linkToRecord(acquisition);
    linkToRecord(prod);
    linkToRecord(transfer);
    return this.model;
  }
}
