package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;

public class ImatexConverter extends Converter {
//  private static final String DOC_BASE_URI = "http://imatex.cdmt.cat/_cat/fitxa_fitxa.aspx?num_id=";

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("Imatex converter require files in JSON format.");

    String mainLang = file.getName().replace(".json", "").split("_")[1];
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

    id = file.getName().replace(".json", "");

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
    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Register number"));


    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .peek(image -> image.addInternalUrl("imatex"))
            .forEach(this::linkToRecord);


    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("CRONOLOGIA*").forEach(prod::addTimeAppellation);
    s.getMulti("MATÈRIES*").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("ORIGEN*").forEach(prod::addPlace);
    s.getMulti("TÈCNICA*").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("CLASSIFICACIÓ GENÈRICA*")
            .map(x -> obj.addClassification(x, "Denomination", "en"))
            .forEach(this::linkToRecord);
    s.getMulti("DENOMINACIÓ*")
            .map(x -> obj.addClassification(x, "Domain", "en"))
            .forEach(this::linkToRecord);
    s.getMulti("DECORACIÓ*").forEach(subject -> obj.addSubject(subject, mainLang));
    s.getMulti("DESTÍ DÚS*").forEach(obj::addIntention);

    String cdt = s.get("ESTAT DE CONSERVACIÓ*");
    if (cdt != null) {
      ConditionAssessment conditionAssessment = new ConditionAssessment(regNum);
      conditionAssessment.concerns(obj);
      conditionAssessment.addCondition("condition", cdt, mainLang);
      linkToRecord(conditionAssessment);
    }

    String rest = s.get("RESTAURACIÓ*");
    if (rest != null) {
      Modification modification = new Modification(regNum, "restoration", rest);
      modification.of(obj);
      linkToRecord(modification);
    }

    linkToRecord(obj.addMeasure(s.get("MEASUREMENT")));
    linkToRecord(obj.addObservation(s.get("DESCRIPTION"), "Description", mainLang));
    linkToRecord(obj.addObservation(s.get("TECHNICAL DESCRIPTION"), "Technical description", mainLang));

    String acquisitionFrom = s.get("FONT INGRÉS*");
    String acquisitionType = s.get("FORMA INGRÉS*");
    String acquisitionDate = s.get("YEAR ENTERED THE MUSEUM");
    LegalBody museum = null;
    if (museumName != null)
      museum = new LegalBody(museumName);

    Acquisition acquisition = new Acquisition(regNum);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setDate(acquisitionDate);
    acquisition.setType(acquisitionType);

    // From the mapping:
    // This field cannot be systematically mapped this way. The relation depends on the record itself
    String npa = s.get("NOMS PROPIS ASSOCIATS");
    if (!StringUtils.isBlank(npa)) {
      Activity activity = new Activity(regNum, "npa");
      activity.addActor(npa);
      linkToRecord(activity);
    }

    prod.addActivity(s.get("DESSIGNER"), "Dessigner");
    prod.addActivity(s.get("MANUFACTURER"), "Manufacturer");
    prod.addActivity(s.get("TAILOR/COUTURIER"), "Tailor/Couturier");
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
