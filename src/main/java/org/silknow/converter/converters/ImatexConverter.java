package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;

public class ImatexConverter extends Converter {
  private static final String DOC_BASE_URI = "http://imatex.cdmt.cat/_cat/fitxa_fitxa.aspx?num_id=";

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
    s.setMultiSeparator(" -");

    // Create the objects of the graph
    logger.trace("creating objects");
    String id = s.get("REGISTER NUMBER");
    String museumName = s.get("MUSEUM");
    LegalBody museum = new LegalBody(museumName);

    Document doc = new Document(id);
    doc.setSource(DOC_BASE_URI + file.getName().replaceFirst(".json$", ""));

    ManMade_Object obj = new ManMade_Object(id);
    obj.addComplexIdentifier(id, "Register number", museum, doc);

    Image img = new Image(s.get("ID FOTOGRAFIA"));
    obj.add(img);


    Production prod = new Production(id);
    prod.add(obj);

    s.getMulti("CRONOLOGIA*").forEach(prod::addTimeAppellation);
    s.getMulti("MATÈRIES*").forEach(prod::addMaterial);
    s.getMulti("ORIGEN*").forEach(prod::addPlace);
    s.getMulti("TÈCNICA*").forEach(prod::addTechnique);
    s.getMulti("CLASSIFICACIÓ GENÈRICA*").forEach(x -> obj.addClassification(x, "denomination"));
    s.getMulti("DECORACIÓ*").forEach(obj::addSubject);
    s.getMulti("DENOMINACIÓ*").forEach(x -> obj.addClassification(x, "domain", museum));
    s.getMulti("DESTÍ DÚS*").forEach(obj::addIntention);

    String cdt = s.get("ESTAT DE CONSERVACIÓ*");
    if (cdt != null) {
      ConditionAssestment conditionAssestment = new ConditionAssestment(id);
      conditionAssestment.assestedBy(museum);
      conditionAssestment.concerns(obj);
      conditionAssestment.addCondition("condition", cdt, "ca");
      conditionAssestment.getConditions().forEach(doc::document);
    }

    String rest = s.get("RESTAURACIÓ*");
    if (rest != null) {
      Modification modification = new Modification(id, "restoration", rest);
      modification.of(obj);
      doc.getModel().add(modification.getModel());
    }

    obj.addMeasure(s.get("MEASUREMENT"));
    obj.associate(s.get("NOMS PROPIS ASSOCIATS"));
    obj.addNote(s.get("DESCRIPTION"), "ca");
    obj.addNote(s.get("TECHNICAL DESCRIPTION"), "ca");

    String acquisitionFrom = s.get("FONT INGRÉS*");
    String acquisitionType = s.get("FORMA INGRÉS*");
    String acquisitionDate = s.get("YEAR ENTERED THE MUSEUM");

    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setDate(acquisitionDate);
    acquisition.setType(acquisitionType);

    prod.addActivity(s.get("DESSIGNER"), "dessigner");
    prod.addActivity(s.get("MANUFACTURER"), "manufacturer");
    prod.addActivity(s.get("TAILOR/COUTURIER"), "tailor/couturier");
    prod.addActivity(s.get("AUTHOR"), "author");

    doc.document(prod);

    Transfer transfer = new Transfer(id);
    transfer.of(obj).by(museum);


    Model m = obj.getModel();
    m.add(doc.getModel());
    m.add(acquisition.getModel());
    m.add(prod.getModel());
    m.add(transfer.getModel());
    return m;
  }
}
