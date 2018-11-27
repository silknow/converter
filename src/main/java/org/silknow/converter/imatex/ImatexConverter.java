package org.silknow.converter.imatex;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.Converter;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileNotFoundException;

public class ImatexConverter extends Converter {
  private static final String DOC_BASE_URI = "http://imatex.cdmt.cat/_cat/fitxa_fitxa.aspx?num_id=";
  private static final String MUSEUM_NAME = "Centre de Documentació i Museu Tèxtil";

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

    Document doc = new Document(id, "imatex");
    doc.setSource(DOC_BASE_URI + file.getName().replaceFirst(".json$", ""));

    ManMade_Object obj = new ManMade_Object(id, "imatex");
    obj.addComplexIdentifier(id, "internal", MUSEUM_NAME, doc);

    Image img = new Image(s.getValue("ID FOTOGRAFIA"), "imatex");
    obj.add(img);


    Production prod = new Production(id, "imatex");
    prod.add(obj);

    s.getMultiValue("CRONOLOGIA*").forEach(prod::addTimeAppellation);
    s.getMultiValue("MATÈRIES*").forEach(prod::addMaterial);
    s.getMultiValue("ORIGEN*").forEach(prod::addPlace);
    s.getMultiValue("CLASSIFICACIÓ GENÈRICA*").forEach(obj::addClassification);
    s.getMultiValue("DECORACIÓ*").forEach(obj::addSubject);
    s.getMultiValue("DENOMINACIÓ*").forEach(x -> obj.addClassification(x, MUSEUM_NAME));
    s.getMultiValue("DESTÍ DÚS*").forEach(obj::addIntention);

    String cdt = s.getValue("ESTAT DE CONSERVACIÓ*");
    if (cdt != null) {
      Condition condition = new Condition(id, "imatex", cdt);
      condition.assestedBy(MUSEUM_NAME);
      condition.concerns(obj);
      doc.document(condition);
    }

    String rest = s.getValue("RESTAURACIÓ*");
    if (rest != null) {
      Modification modification = new Modification(id, "imatex", rest);
      modification.of(obj);
      doc.getModel().add(modification.getModel());
    }


    String acquisitionFrom = s.getValue("FONT INGRÉS*");
    String acquisitionType = s.getValue("FORMA INGRÉS*");

    Acquisition acquisition = new Acquisition(id, "imatex");
    acquisition.transfer(acquisitionFrom, obj, MUSEUM_NAME);
    acquisition.setType(acquisitionType);

    String npa = s.getValue("NOMS PROPIS ASSOCIATS");
    obj.associate(npa);


    doc.document(prod);

    Model m = obj.getModel();
    m.add(doc.getModel());
    return m;
  }
}
