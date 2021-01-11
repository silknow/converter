package org.silknow.converter.entities;

import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.string2vocabulary.VocabularyManager;
import org.silknow.converter.commons.StopWordException;
import org.silknow.converter.ontologies.CIDOC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Production extends Entity {
  private static final String CIRCA_REGEX = "(?i)(circa|around|about|posiblemente|(proba|possi)bly|ca?\\.|\\[ca])";
  private static final String UNCONFIRMED_REGEX = "\\.\\.\\. (unconfirmed|(sin|sense) confirmar)";
  private static final String UNKNOWN_REGEX = "(?i)(de[sc]+ono?cid[oa]\\.?|n\\.d\\.?|no)";
  private static final Pattern UNKNOWN_PATTERN = Pattern.compile(UNKNOWN_REGEX);
  private static final String ANY_BRACKETS = "[(\\[\\])]";

  private int tsCount;
  private boolean timeUnconfirmed;
  private String timeModifier;

  //List<Place> placeList = new ArrayList<>();
  //public List<Place> getPlaces() {return this.placeList;}

  public Production(String id) {
    super(id);
    this.setClass(CIDOC.E12_Production);

    tsCount = 0;
    timeUnconfirmed = false;
    timeModifier = "";
  }

  public void addTimeAppellation(String time) {
    boolean timeApproximate = false;

    if (time == null) return;
    time = time.trim();

    Matcher m = UNKNOWN_PATTERN.matcher(time);
    if (m.find()) {
      time = time.replace(m.group(1), "").trim();
      if (time.matches(ANY_BRACKETS + ".*" + ANY_BRACKETS)) {
        time = time.replaceAll(ANY_BRACKETS, "").trim();
      }
    }
    if (time.isEmpty()) return;

    if (time.matches(UNCONFIRMED_REGEX)) {
      timeUnconfirmed = true;
      return;
    }
    if (time.contains("?"))
      timeUnconfirmed = true;

    if (time.matches(".*" + CIRCA_REGEX + ".*"))
      timeApproximate = true;

    // we mostly use replaceAll in order to enable case insensitive (?i) replacements
    time = time.replaceAll("\\s+", " ");
    time = time.replaceAll("\\(.*\\)", ""); // curly brackets
    time = time.replaceAll("\\[.*]", ""); // square brackets
    time = time.replaceAll(ANY_BRACKETS, ""); // orphan brackets

    time = time.replace("?", "");
    time = time.replaceAll(CIRCA_REGEX, "");
    time = time.replaceAll("\"", "");
    time = time.replaceAll("\\.0$", ""); // workaround some dates as 1960.0
    time = time.replaceAll("\\.$", ""); // trailing dots
    time = time.trim();
    if (time.isEmpty()) return;

    for (String regex : TimeSpan.CENTURY_PART_REGEXES) { // case "last quarter"
      if (time.matches(regex)) {
        // this will be added to next line"
        timeModifier = time + " ";
        return;
      }
    }

    TimeSpan ts = new TimeSpan(timeModifier + time);
    // centralising the ts definition, those values are incorrect
    // if (timeUnconfirmed) ts.addNote("unconfirmed", "en");
    // if (timeApproximate) ts.addNote("approximate", "en");

    this.addTimeSpan(ts);
    tsCount++;
  }


  public Production add(ManMade_Object obj) {
    this.addProperty(CIDOC.P108_has_produced, obj);
    return this;
  }

  public void addMaterial(String material, String lang) {
    Resource result = VocabularyManager.searchInCategory(material, null, "thesaurus", false);
    if (result != null) {
      ResIterator resIterator = result.getModel().listResourcesWithProperty(SKOS.member, result);
      if (resIterator.hasNext()) {
        Resource collection_level2 = resIterator.next();
        ResIterator resIterator2 = result.getModel().listResourcesWithProperty(SKOS.member, collection_level2);
        String collection;
        if (resIterator2.hasNext()) {
          Resource collection_level1 = resIterator2.next();
          collection = collection_level1.getURI();
        } else {
          collection = collection_level2.getURI();
        }
        if (collection.contains("materials") || collection.contains("300264091")) {
          this.addProperty(CIDOC.P126_employed, result);
        } else if (collection.contains("techniques") || collection.contains("300264090")) {
          this.addProperty(CIDOC.P32_used_general_technique, result);
        } else {
          result = null;
        }
      }
    }
    if (result == null) {
      //System.out.println("Material not found in vocabularies: " + technique);
      this.addProperty(CIDOC.P126_employed, material, lang);
    }
  }


  public void addPlace(String place) {
    if (place.startsWith("... "))
      // TODO something?
      return;

    try {
      this.addPlace(new Place(place));
    } catch (StopWordException e) {
      // no further action required
    }
  }

  public void addPlace(Place place) {
    this.addProperty(CIDOC.P8_took_place_on_or_within, place);
    //this.placeList.add(place);
  }


  public void addTechnique(String technique, String lang) {
    Resource result = VocabularyManager.searchInCategory(technique, null, "thesaurus", false);
    if (result != null) {
      ResIterator resIterator = result.getModel().listResourcesWithProperty(SKOS.member, result);
      if (resIterator.hasNext()) {
        Resource collection_level2 = resIterator.next();
        ResIterator resIterator2 = result.getModel().listResourcesWithProperty(SKOS.member, collection_level2);
        String collection;
        if (resIterator2.hasNext()) {
          Resource collection_level1 = resIterator2.next();
          collection = collection_level1.getURI();
        } else {
          collection = collection_level2.getURI();
        }
        if (collection.contains("techniques") || collection.contains("300264090")) {
          this.addProperty(CIDOC.P32_used_general_technique, result);

        } else if (collection.contains("materials") || collection.contains("300264091")) {
          this.addProperty(CIDOC.P126_employed, result);
        } else {
          result = null;
        }
      }
    }
    if (result == null) {
      //System.out.println("Technique not found in vocabularies: " + material);
      this.addProperty(CIDOC.P32_used_general_technique, technique, lang);
    }
  }

  public void addTool(ManMade_Object tool) {
    this.addProperty(CIDOC.P16_used_specific_object, tool);
  }

  public void addUsedObject(String used_object, String lang) {
    this.addProperty(CIDOC.P125_used_object_of_type, used_object, lang);
  }

  public void addActor(Actor actor) {
    this.addProperty(CIDOC.P14_carried_out_by, actor);
  }


  public boolean hasTimeSpans() {
    return tsCount > 0;
  }
}
