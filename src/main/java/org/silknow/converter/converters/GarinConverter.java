package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.silknow.converter.entities.*;
import org.silknow.converter.ontologies.CIDOC;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GarinConverter extends Converter {
  private final static String MEDIA_BASE = "https://silknow.org/silknow/media/garin/";
  private final static Pattern ANV_REV = Pattern.compile("(ANV|REV|DET)");
  public static final Map<String, String> ANV_REV_TABLE;

  private static final String Pattern_unit_REGEX = "Rapport: (\\d+(?:\\.\\d+)?) cm";
  private static final Pattern Pattern_unit_PATTERN = Pattern.compile(Pattern_unit_REGEX);

  static {
    Hashtable<String, String> tmp = new Hashtable<>();
    tmp.put("ANV", "recto"); // anverso
    tmp.put("REV", "verso"); // reverso
    tmp.put("DET", "detail");
    ANV_REV_TABLE = Collections.unmodifiableMap(tmp);
  }



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
      filename = file.getName();
    else filename = null;
    if (filename == null)
      return null;

    //if (s.get("Nº Inventario").isEmpty()  != true ) {
    String[] path = file.getAbsolutePath().split("/");
    String regNum = (path[path.length - 3] + "_" + path[path.length - 2] + "_" + s.get("Nº Inventario")).replaceAll(" +", "_");
    id = regNum;

    //}

    //else {
    //String regNum = filename+" filenameID";
    //id = regNum;
    //}


    String ownerName = s.get("Propiedad");
    LegalBody owner = null;
    if (ownerName != null)
      owner = new LegalBody(ownerName);

    LegalBody GARIN = new LegalBody("GARIN");

    ManMade_Object obj = new ManMade_Object(id);
    obj.addTitle(s.get("Denominación Principal"), mainLang);


/*
    if ((!s.getMulti("Denominación Principal").findAny().isPresent())) {

      final List<String> terms = new ArrayList<String>();
      terms.add((s.getMulti("Tipología").findFirst().orElse(null)));
      terms.add((s.getMulti("Época").findFirst().orElse(null)));
      terms.add("Valencia");
      final String constrlabel = terms
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.joining(", "));
      obj.addConstructedTitle(constrlabel, mainLang);
    }

 */


    linkToRecord(obj.addClassification(s.get("Objecto"), "Objecto", mainLang, GARIN));
    linkToRecord(obj.addObservation(s.get("Descripción"), "Descripción", mainLang));
    linkToRecord(obj.addObservation(s.get("Descripción técnica"), "Descripción técnica", mainLang));

    /*
    String dim = s.get("Descripción técnica");
    if (dim != null) {
      Matcher matcher5 = Pattern_unit_PATTERN.matcher(dim);
      if (matcher5.find()) {
        linkToRecord(obj.addPatternMeasure(matcher5.group(2), matcher5.group(1)));
      }
    }
    */




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

    Move mo = new Move(regNum);
    mo.to(s.getMulti("Ubicación").findFirst().orElse(null));

    String rest = s.get("Restauraciones localizadas");
    if (rest != null) {
      Modification modification = new Modification(regNum, rest);
      modification.of(obj);
      linkToRecord(modification);
    }




      Transfer transfer = new Transfer(id);
      transfer.of(obj).by("chalet garin");

      Production prod = new Production(id);
      prod.add(obj);
      s.getMulti("Técnica").forEach(used_object -> prod.addUsedObject(used_object, mainLang));
      s.getMulti("Tipología").forEach(technique -> prod.addTechnique(technique, mainLang));
      s.getMulti("Tipología")
        .map(x -> obj.addClassification(x, "Tipología", "es"))
        .forEach(this::linkToRecord);
      prod.addActivity(s.get("Autor de la obra"), "author");
      prod.addTimeAppellation(s.get("Época"));
      s.getMulti("Material").forEach(material -> prod.addMaterial(material, mainLang));
      //s.getMulti("Accessorios").map(ManMade_Object::new).forEach(prod::addTool);
      prod.addPlace("valencia");


      try {
        Path configFilePath = FileSystems.getDefault().getPath(file.getParent()).getParent().getParent();
        Stream<Path> fileWithName = Files.walk(configFilePath, Integer.MAX_VALUE);

        //long count = fileWithName.count();
        //System.out.println(Integer.MAX_VALUE + "..." + count);

        if (s.get("Nº Inventario") != null) {
          List<String> filenamelist = fileWithName
            .filter(f -> f.getFileName().toString().matches("^" + s.get("Nº Inventario").replaceAll("[. ]", "") + "[ .].+$"))
            .filter(f -> !f.toString().endsWith("xls"))
            .map(Path::getFileName)
            .map(Path::toString)
            .map(x -> x.replaceAll(" +", "_")) // singe/double space to single underscore
            .map(x -> x.replaceAll("(?i)\\.jpg$", ".jpg")) // replace uppercase .JPG
            .collect(Collectors.toList());

          //if (filenamelist.isEmpty()) {
          //System.out.println(s.get("Nº Inventario").replaceAll("[. ]", ""));
          //}


          for (String name : filenamelist) {
            Matcher matcher = ANV_REV.matcher(name); // search for anverso/reverso

            Image img = new Image();
            img.setContentUrl(MEDIA_BASE + name);

            if (matcher.find())
              img.addProperty(CIDOC.P2_has_type, ANV_REV_TABLE.get(matcher.group(1)));

            obj.add(img);
          }
        }
      } catch (IOException e) {
        logger.error(e.getLocalizedMessage());
      }

      for (String key : Arrays.asList("Anverso", "Reverso")) {
        String section = s.get(key);
        if (section == null || section.equalsIgnoreCase("no")) continue;
        linkToRecord(obj.addInfo(key, section, "es"));
      }

      linkToRecord(obj);
      linkToRecord(transfer);
      linkToRecord(prod);
      if (owner != null) linkToRecord(owner);
      linkToRecord(acquisition);
      linkToRecord(conditionAssessment);
      return this.model;
    }

  }
