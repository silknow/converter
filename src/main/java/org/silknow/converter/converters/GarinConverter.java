package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.entities.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    if (!file.getName().contains(" "))
       id = file.getName().replace(".xls","");
    else id = null;
    if (id == null)
      return null;


    String ownerName = s.get("Propiedad");
    LegalBody owner = null;
    if (ownerName != null)
      owner = new LegalBody(ownerName);

    LegalBody GARIN = new LegalBody("GARIN");


    ManMade_Object obj = new ManMade_Object(id);
    obj.addTitle(s.get("Denominacion principal"));
    linkToRecord(obj.addComplexIdentifier(id, "Register number", owner));
    linkToRecord(obj.addClassification(s.get("Objecto"), "Domain", "en", GARIN));
    linkToRecord(obj.addClassification(s.get("Tipología"), "Denomination", "en", owner));
    linkToRecord(obj.addObservation(s.get("Descripción"),  "Descripción", mainLang));
    linkToRecord(obj.addObservation(s.get("Descripción técnica"), "Descripción técnica" , mainLang));

    try {
      linkToRecord(obj.addMeasure(s.get("Medidas")));
    } catch (RuntimeException re) {
      logger.error(re.getMessage());
    }


    ConditionAssessment conditionAssessment = new ConditionAssessment(id);
    conditionAssessment.assestedBy(owner);
    conditionAssessment.concerns(obj);

    conditionAssessment.addCondition("Condition", s.get("Condición"), mainLang);
    conditionAssessment.addCondition("Deterioration", s.get("Deterioros"), mainLang);
    conditionAssessment.addCondition("Missing parts", s.get("Partes que faltan"), mainLang);


    Acquisition acquisition = new Acquisition(id);
    acquisition.transfer(null, obj, owner);

    String rest = s.get("Restauraciones localizadas");
    if (rest != null && !rest.equalsIgnoreCase("no")) {
      Modification modification = new Modification(id, "restoration", rest);
      modification.of(obj);
      linkToRecord(modification);
    }




    Move move = new Move(id);
    move.of(obj).from("chalet garin").to(s.get("Ubicación"));

    Production prod = new Production(id);
    prod.add(obj);
    s.getMulti("Técnica").forEach(technique -> prod.addTechnique(technique, mainLang));
    prod.addActivity(s.get("Autor de la obra"), "author");
    prod.addTimeAppellation(s.get("Época"));
    s.getMulti("Material").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Accessorios").map(ManMade_Object::new).forEach(prod::addTool);
    prod.addPlace("chalet garin");




   try {
       Path configFilePath = FileSystems.getDefault().getPath(file.getParent());
       Stream<Path> fileWithName = Files.walk(configFilePath);
       List<String> filenamelist;
       {

           filenamelist = fileWithName
                   .filter(f -> f.getFileName().toString().startsWith(id.replaceAll("[. ]", "")+" ") || f.getFileName().toString().startsWith(id.replaceAll("[. ]", "")+"."))
                   .filter(f -> !f.toString().endsWith("xls"))
                   .map(Path::getFileName)
                   .map(Path::toString)
                   .map(x -> x.replace(".jpg",""))
                   .collect(Collectors.toList());
           System.out.println(filenamelist);

       }

       Set<String> fileset = new LinkedHashSet<>();
       for (String str : filenamelist) {
           String value = str;
           // Iterate as long as you can't add the value indicating that we have
           // already the value in the set
           for (int i = 1; !fileset.add(value); i++) {
               value = str + i;
           }
       }

       for (String name : fileset) {
           System.out.println(name);

           //todo ANV REV then ...

           Image img = new Image();
           img.setContentUrl("http://silknow.org/silknow/media/garin/" + name.toString().replace(" ", "_") + ".jpg");
           obj.add(img);
       }
   } catch (IOException e) {
     logger.error(e.getLocalizedMessage());
   }


    //int imgCount = 0;
    //for (byte[] x : s.getImages()) {
      //Image img = new Image(id + imgCount++);
      //String imgName = id.replaceAll(" ", "_") + imgCount + ".jpg";

      //try {
        //OutputStream out = new FileOutputStream(Main.outputFolder + "/img/" + imgName);
        //out.write(x);
        //out.close();
        //img.setContentUrl("http://silknow.org/silknow/media/garin/" + imgName);
      //} catch (IOException e) {
        //logger.error(e.getLocalizedMessage());
      //}

      //obj.add(img);
      //linkToRecord(img);
    //}

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
    linkToRecord(conditionAssessment);
    return this.model;
  }

}
