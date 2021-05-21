package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElTesoroConverter extends Converter {

  private static final String DIMENSION_REGEX = "(\\d+(?:\\.\\d+)?) × (\\d+(?:\\.\\d+)?) cm.";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("ElTesoro converter requires files in JSON format.");

    String mainLang = "es";
    this.DATASET_NAME = "el-tesoro";

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

    filename = file.getName();

    String museumName = "Museo de Arte Sacro El Tesoro de la Concepción";

    String regNum = s.get("Nº de Inventario");
    if (regNum == null)
      regNum = s.getId();
    id = s.getId();

    ManMade_Object obj = new ManMade_Object(regNum);
    s.getMulti("Denominación")
            .map(x -> obj.addClassification(x, "Denominación", mainLang))
            .forEach(this::linkToRecord);



    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("el-tesoro"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);


    s.getMulti("Cronología").forEach(prod::addTimeAppellation);
    s.getMulti("Origin").forEach(prod::addPlace);
    s.getMulti("Materiales").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Técnica").forEach(material -> prod.addTechnique(material, mainLang));
    linkToRecord(obj.addObservation(s.get("Otros datos de interés"), "Otros datos de interés", mainLang));
    //linkToRecord(obj.addProperty(OWL.sameAs, this.model.createResource(s.getUrl())));

    String author = s.get("Autor");
    Actor actor = new Actor(author);
    prod.addActivity(actor, "Author");



    String dim = s.get("Dimensiones");
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }

    LegalBody museum = new LegalBody(museumName);

    String cdt = s.get("Estado de conservación");
    if (cdt != null) {
      ConditionAssessment conditionAssessment = new ConditionAssessment(regNum);
      conditionAssessment.concerns(obj);
      conditionAssessment.addCondition("Estado de conservación", cdt, mainLang);
      linkToRecord(conditionAssessment);
    }

    Inscription ins = new Inscription((s.get("Marcas")), "es");

    obj.add(ins);



    linkToRecord(obj);
    linkToRecord(prod);
    return this.model;
  }

  private void write(String text, String file) throws IOException {
    if (StringUtils.isBlank(text)) return;
    FileWriter fWriter = new FileWriter(file, true);
    BufferedWriter bWriter = new BufferedWriter(fWriter);
    bWriter.write("- " + text + "\n");
    bWriter.close();
    fWriter.close();
  }
}
