package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GarinConverter extends Converter {
  private static final String GARIN = "imatex";

  @Override
  public boolean canConvert(File file) {
    return isExcel(file);
  }

  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("Garin converter require files in XLS (Excel) format.");

    // Parse XLS
    logger.trace("parsing XLS");
    GarinRecord s;
    try {
      s = GarinRecord.from(file);
    } catch (IOException e) {
      // cannot happen
      e.printStackTrace();
      return null;
    }

    // Create the objects of the graph
    logger.trace("creating objects");
    String id = s.get("Nº Inventario");
    String ownerName = s.get("Propiedad");
    LegalBody owner = new LegalBody(ownerName);

    Document doc = new Document(id, GARIN);

    ManMade_Object obj = new ManMade_Object(id, GARIN);
    obj.addComplexIdentifier(id, "Register number", owner, doc);
    obj.addTitle(s.get("Denominacion principal"));
    obj.addClassification(s.get("Objecto"), "domain", owner);
    obj.addClassification(s.get("Tipologia"), "denomination", owner);
    obj.addMeasure(s.get("Medidas"));
    obj.addNote(s.get("Descripción"), "es");
    obj.addNote(s.get("Descripción técnica"), "es");

    ConditionAssestment conditionAssestment = new ConditionAssestment(id, GARIN);
    conditionAssestment.assestedBy(owner);
    conditionAssestment.concerns(obj);

    conditionAssestment.addCondition("condition", s.get("Condición"), "es");
    conditionAssestment.addCondition("deterioration", s.get("Deterioros"), "es");
    conditionAssestment.addCondition("missing parts", s.get("Partes que faltan"), "es");
    conditionAssestment.getConditions().forEach(doc::document);


    String rest = s.get("Restauraciones localizadas");
    if (rest != null && !rest.equalsIgnoreCase("no")) {
      Modification modification = new Modification(id, GARIN, "restoration", rest);
      modification.of(obj);
      doc.getModel().add(modification.getModel());
    }


    Move move = new Move(id, GARIN);
    move.of(obj).from(s.get("Localización")).to(s.get("Ubicación"));

    Production prod = new Production(id, GARIN);
    prod.add(obj);
    prod.addTechnique(s.get("Tecnica"));
    prod.addActivity(s.get("Autor de la obra"), "author");
    prod.addTimeAppellation(s.get("Época"));
    s.getMulti("Material").forEach(prod::addMaterial);
    s.getMulti("Accessorios").map(x -> new ManMade_Object(x, GARIN)).forEach(prod::addTool);


    for (String key : Arrays.asList("Anverso", "Reverso")) {
      String section = s.get(key);
      if (section == null || section.equalsIgnoreCase("no")) continue;
      obj.addInfo(key, section, "es");
    }

    Model m = obj.getModel();
    m.add(doc.getModel());
    m.add(move.getModel());
    m.add(prod.getModel());
    return m;

  }
}
