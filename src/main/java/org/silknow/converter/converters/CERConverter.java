package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CERConverter extends Converter {

  private static final String DIMENSION_REGEX = "Altura = (\\d+?) cm; Anchura = (\\d+?) cm";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("CERConverter require files in JSON format.");

    String mainLang = "es";
    this.DATASET_NAME = "CER";

    // Parse JSON
    logger.trace("parsing json");
    CrawledJSON s;
    try {
      s = CrawledJSON.from(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    //s.setMultiSeparator(" -");

    // Create the objects of the graph
    logger.trace("creating objects");

    id = file.getName().replace(".json", "");

    String museumName = s.get("Museo");

    String regNum = s.get("Inventario");
    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Inventario"));
    obj.addTitle(s.getMulti("Título").findFirst().orElse(null));


    Production prod = new Production(regNum);
    prod.add(obj);

    s.getMulti("Iconografia").forEach(subject -> obj.addSubject(subject, mainLang));
    s.getMulti("Datación").forEach(prod::addTimeAppellation);
    s.getMulti("Contexto Cultural/Estilo").forEach(prod::addTimeAppellation);
    s.getMulti("Materia/Soporte").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Lugar de Producción/Ceca").forEach(prod::addPlace);
    s.getMulti("Técnica").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("Clasificación Genérica")
            .map(x -> obj.addClassification(x, "Clasificación Genérica", mainLang))
            .forEach(this::linkToRecord);

    s.getImages().map(Image::fromCrawledJSON)
            .peek(obj::add)
            .peek(image -> image.addInternalUrl("ceres-mcu"))
            .forEach(this::linkToRecord);

    String dim = s.getMulti("Dimensiones").findFirst().orElse(null);
    if (dim != null) {
      Matcher matcher = DIMENSION_PATTERN.matcher(dim);
      if (matcher.find()) {
        linkToRecord(obj.addMeasure(matcher.group(2), matcher.group(1)));
      }
    }

    Right copyphoto = new Right(obj.getUri() + "/image/right");
    copyphoto.addNote(s.get("Photographie:"));
    s.getMulti("Photographie:", ", ")
            .map(x -> x.replaceFirst("© ", ""))
            .map(Actor::new)
            .forEach(copyphoto::ownedBy);

    linkToRecord(obj.addObservation(s.get("Descripción"), "Descripción", mainLang));
    linkToRecord(obj.addObservation(s.get("Objeto/Documento"), "Objeto/Documento", mainLang));
    linkToRecord(obj.addObservation(s.get("Clasificación Razonada"), "Clasificación Razonada", mainLang));

    LegalBody museum = null;
    if (museumName != null)
      museum = new LegalBody(museumName);

    Acquisition acquisition = new Acquisition(regNum);


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(museum);

    if (s.get("Bibliografía") != null) {
      InformationObject bio = new InformationObject(regNum + "b");
      bio.setType("Bibliografía", mainLang);
      bio.isAbout(obj);
      bio.addNote(s.get("Bibliografía"), mainLang);
      linkToRecord(bio);
    }


    linkToRecord(obj);
    linkToRecord(acquisition);
    linkToRecord(prod);
    linkToRecord(transfer);
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
