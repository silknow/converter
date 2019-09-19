package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.Main;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class GarinConverter extends Converter {
  @Override
  public boolean canConvert(File file) {
    return isExcel(file);
  }

  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());

    if (!this.canConvert(file))
      throw new RuntimeException("Garin converter require files in XLS (Excel) format.");

    String mainLang = "es";
    this.DATASET_NAME = "garin";

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
    id = s.get("Nº Inventario");
    if (id == null)
      return null;


    String ownerName = s.get("Propiedad");
    LegalBody owner = null;
    if (ownerName != null)
      owner = new LegalBody(ownerName);

    ManMade_Object obj = new ManMade_Object(id);
    obj.addTitle(s.get("Denominacion principal"));
    linkToRecord(obj.addComplexIdentifier(id, "Register number", owner));
    linkToRecord(obj.addClassification(s.get("Objecto"), "Domain", mainLang, owner));
    linkToRecord(obj.addClassification(s.get("Tipología"), "Denomination",mainLang, owner));
    linkToRecord(obj.addObservation(s.get("Descripción"), mainLang, "descripción"));
    linkToRecord(obj.addObservation(s.get("Descripción técnica"), mainLang, "descripción técnica"));

    try {
      linkToRecord(obj.addMeasure(s.get("Medidas")));
    } catch (RuntimeException re) {
      logger.error(re.getMessage());
    }


    ConditionAssestment conditionAssestment = new ConditionAssestment(id);
    conditionAssestment.assestedBy(owner);
    conditionAssestment.concerns(obj);

    conditionAssestment.addCondition("condition", s.get("Condición"), mainLang);
    conditionAssestment.addCondition("deterioration", s.get("Deterioros"), mainLang);
    conditionAssestment.addCondition("missing parts", s.get("Partes que faltan"), mainLang);


    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(null, obj, owner);

    String rest = s.get("Restauraciones localizadas");
    if (rest != null && !rest.equalsIgnoreCase("no")) {
      Modification modification = new Modification(id, "restoration", rest);
      modification.of(obj);
      linkToRecord(modification);
    }


    Move move = new Move(id);
    move.of(obj).from(s.get("Localización")).to(s.get("Ubicación"));

    Production prod = new Production(id);
    prod.add(obj);
    s.getMulti("Técnica").forEach(technique -> prod.addTechnique(technique, mainLang));
    prod.addActivity(s.get("Autor de la obra"), "author");
    prod.addTimeAppellation(s.get("Época"));
    s.getMulti("Material").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Accessorios").map(ManMade_Object::new).forEach(prod::addTool);

    int imgCount = 0;
    for (byte[] x : s.getImages()) {
      Image img = new Image(id + imgCount++);
      String imgName = id.replaceAll(" ", "_") + imgCount + ".jpg";

      try {
        OutputStream out = new FileOutputStream(Main.outputFolder + "/img/" + imgName);
        out.write(x);
        out.close();
        img.setContentUrl("http://silknow.org/silknow/media/garin/" + imgName);
      } catch (IOException e) {
        logger.error(e.getLocalizedMessage());
      }

      obj.add(img);
      linkToRecord(img);
    }

    for (String key : Arrays.asList("Anverso", "Reverso")) {
      String section = s.get(key);
      if (section == null || section.equalsIgnoreCase("no")) continue;
      linkToRecord(obj.addInfo(key, section, "es"));
    }

    linkToRecord(obj);
    linkToRecord(move);
    linkToRecord(prod);
    linkToRecord(owner);
    linkToRecord(acquisition);
    linkToRecord(conditionAssestment);
    return this.model;
  }

}
