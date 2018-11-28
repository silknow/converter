package org.silknow.converter.imatex;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.Converter;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;

public class ImatexConverter extends Converter {
  private static final String IMATEX = "imatex";
  private static final String DOC_BASE_URI = "http://imatex.cdmt.cat/_cat/fitxa_fitxa.aspx?num_id=";

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());

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
    String id = s.getValue("REGISTER NUMBER");
    String museumName = s.getValue("MUSEUM");
    LegalBody museum = new LegalBody(museumName);

    Document doc = new Document(id, IMATEX);
    doc.setSource(DOC_BASE_URI + file.getName().replaceFirst(".json$", ""));

    ManMade_Object obj = new ManMade_Object(id, IMATEX);
    obj.addComplexIdentifier(id, "Register number", museum, doc);

    Image img = new Image(s.getValue("ID FOTOGRAFIA"), IMATEX);
    obj.add(img);


    Production prod = new Production(id, IMATEX);
    prod.add(obj);

    s.getMultiValue("CRONOLOGIA*").forEach(prod::addTimeAppellation);
    s.getMultiValue("MATÈRIES*").forEach(prod::addMaterial);
    s.getMultiValue("ORIGEN*").forEach(prod::addPlace);
    s.getMultiValue("TÈCNICA*").forEach(prod::addTechnique);
    s.getMultiValue("CLASSIFICACIÓ GENÈRICA*").forEach(x -> obj.addClassification(x, "denomination"));
    s.getMultiValue("DECORACIÓ*").forEach(obj::addSubject);
    s.getMultiValue("DENOMINACIÓ*").forEach(x -> obj.addClassification(x, "domain", museum));
    s.getMultiValue("DESTÍ DÚS*").forEach(obj::addIntention);

    String cdt = s.getValue("ESTAT DE CONSERVACIÓ*");
    if (cdt != null) {
      Condition condition = new Condition(id, IMATEX, cdt);
      condition.assestedBy(museum);
      condition.concerns(obj);
      doc.document(condition);
    }

    String rest = s.getValue("RESTAURACIÓ*");
    if (rest != null) {
      Modification modification = new Modification(id, IMATEX, "restauració", rest);
      modification.of(obj);
      doc.getModel().add(modification.getModel());
    }

    obj.addMeasure(s.getValue("MEASUREMENT"));
    obj.associate(s.getValue("NOMS PROPIS ASSOCIATS"));
    obj.addNote(s.getValue("DESCRIPTION"), "ca");
    obj.addNote(s.getValue("TECHNICAL DESCRIPTION"), "ca");

    String acquisitionFrom = s.getValue("FONT INGRÉS*");
    String acquisitionType = s.getValue("FORMA INGRÉS*");
    String acquisitionDate = s.getValue("YEAR ENTERED THE MUSEUM");

    Acquisition acquisition = new Acquisition(id, IMATEX);
    acquisition.transfer(acquisitionFrom, obj, museum);
    acquisition.setDate(acquisitionDate);
    acquisition.setType(acquisitionType);

    prod.addActivity(s.getValue("DESSIGNER"), "dessigner");
    prod.addActivity(s.getValue("MANUFACTURER"), "manufacturer");
    prod.addActivity(s.getValue("TAILOR/COUTURIER"), "tailor/couturier");
    prod.addActivity(s.getValue("AUTHOR"), "author");

    doc.document(prod);

    Transfer transfer = new Transfer(id, IMATEX);
    transfer.of(obj).by(museum);


    Model m = obj.getModel();
    m.add(doc.getModel());
    m.add(acquisition.getModel());
    m.add(prod.getModel());
    m.add(transfer.getModel());
    return m;
  }
}
