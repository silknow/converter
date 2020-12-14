package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.silknow.converter.commons.CrawledJSON;
import org.silknow.converter.entities.*;

import java.io.*;
import java.util.regex.Pattern;

public class VeneziaConverter extends Converter {


  @Override
  public boolean canConvert(File file) {
    return isJson(file);
  }


  @Override
  public Model convert(File file) {
    logger.debug("%%% FILE " + file.getName());
    if (!this.canConvert(file))
      throw new RuntimeException("VeneziaConverter require files in JSON format.");

    String mainLang = "it";
    this.DATASET_NAME = "venezia";

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
    String museumName = "Musei Civici Venezia";

    String regNum = s.get("Numero inventario museo");
    id = regNum;

    ManMade_Object obj = new ManMade_Object(regNum);
    linkToRecord(obj.addComplexIdentifier(regNum, "Numero inventario museo"));
    s.getMulti("Definizione")
            .map(x -> obj.addClassification(x, "Definizione", mainLang))
            .forEach(this::linkToRecord);

    /*
    final List<String> terms = new ArrayList<String>();
    terms.add((s.getMulti("titleField").findFirst().orElse(null)));
    terms.add((s.getMulti("displayDateField").findFirst().orElse(null)));
    terms.add((s.getMulti("cultureField").findFirst().orElse(null)));
    final String constrlabel = terms
      .stream()
      .filter(Objects::nonNull)
      .collect(Collectors.joining(", "));
    obj.addConstructedTitle(constrlabel, mainLang);
/*


     */
    s.getImages().map(Image::fromCrawledJSON)
            .peek(image -> image.addInternalUrl("venezia"))
            .peek(obj::add)
            .forEach(this::linkToRecord);

    Production prod = new Production(regNum);
    prod.add(obj);

    String time = s.get("Frazione di secolo")+" "+s.get("Scolo")+" "+s.get("Data inizio")+" "+s.get("Data fine");
    prod.addTimeAppellation(time);
    s.getMulti("Denominazione").forEach(prod::addPlace);


    s.getMulti("Materia e tecnica").forEach(material -> prod.addMaterial(material, mainLang));
    s.getMulti("Classe percorso")
            .map(x -> obj.addClassification(x, "Classe percorso", mainLang))
            .forEach(this::linkToRecord);


        linkToRecord(obj.addMeasure(s.get("Altezza"), s.get("Larghezza")));



    linkToRecord(obj.addObservation(s.get("Indicazioni sull'oggetto"), "Indicazioni sull'oggetto", mainLang));


    LegalBody legalbody = null;
      legalbody = new LegalBody(s.get("Denominazione raccolta"));


    Transfer transfer = new Transfer(regNum);
    transfer.of(obj).by(legalbody);


    linkToRecord(obj);

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
