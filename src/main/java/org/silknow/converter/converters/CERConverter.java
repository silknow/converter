package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    this.DATASET_NAME = "cer";

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

    filename = file.getName();

    String museumName = s.get("Museo");

    String regNum = s.get("Inventario");
    id = regNum;
    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Inventario"));
    obj.addTitle(s.getMulti("Título").findFirst().orElse(null));


    if ((!s.getMulti("Título").findAny().isPresent())) {

      final List<String> terms = new ArrayList<String>();
      terms.add((s.getMulti("Objeto/Documento").findFirst().orElse(null)));
      terms.add((s.getMulti("Datación").findFirst().orElse(null)));
      terms.add((s.getMulti("Lugar de Producción/Ceca").findFirst().orElse(null)));
      final String constrlabel = terms
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));
      obj.addConstructedTitle(constrlabel);
    }



    //s.getMulti("Título")
      //      .map(x -> obj.addClassification(x, "Título", mainLang))
        //    .forEach(this::linkToRecord);



    Production prod = new Production(regNum);
    prod.add(obj);
    prod.addActivity(s.getMulti("Autor").findFirst().orElse(null), s.getMulti("Uso/función").findFirst().orElse(null));



    s.getMulti("Iconografia").forEach(subject -> obj.addSubject(subject, mainLang));
    s.getMulti("Datación").forEach(prod::addTimeAppellation);
    //s.getMulti("Contexto Cultural/Estilo").forEach(prod::addTimeAppellation);
    linkToRecord(obj.addObservation(s.get("Contexto Cultural/Estilo"), "Contexto Cultural/Estilo", mainLang));


    s.getMulti("Materia/Soporte","   ").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Lugar de Producción/Ceca").forEach(prod::addPlace);
    s.getMulti("Técnica","   ").forEach(technique -> prod.addTechnique(technique, mainLang));
    s.getMulti("Clasificación Genérica", ";")
            .map(x -> obj.addClassification(x, "Clasificación Genérica", mainLang))
            .forEach(this::linkToRecord);


    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("ceres-mcu"))
            .peek(obj::add)
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
    //linkToRecord(obj.addObservation(s.get("Objeto/Documento"), "Objeto/Documento", mainLang));
    s.getMulti("Objeto/Documento")
            .map(x -> obj.addClassification(x, "Objeto/Documento", mainLang))
            .forEach(this::linkToRecord);

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

    if (s.getMulti("Tipo de Colección").findFirst().orElse(null) != null) {
      Collection collection = new Collection(regNum, s.getMulti("Tipo de Colección").findFirst().orElse(null));
      collection.of(obj);
      collection.addAppellation(s.getMulti("Tipo de Colección").findFirst().orElse(null));
      linkToRecord(collection);
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
