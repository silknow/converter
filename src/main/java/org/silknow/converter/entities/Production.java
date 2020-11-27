package org.silknow.converter.entities;

import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.string2vocabulary.VocabularyManager;
import org.silknow.converter.commons.StopWordException;
import org.silknow.converter.ontologies.CIDOC;

public class Production extends Entity {
  private int tsCount;
  private boolean timeUnconfirmed;

  public Production(String id) {
    super(id);
    this.setClass(CIDOC.E12_Production);

    tsCount = 0;
    timeUnconfirmed = false;
  }

  public void addTimeAppellation(String timeAppellation) {

    if (timeAppellation == null) return;
    if (timeAppellation.equals("... unconfirmed") | timeAppellation.equals("... sin confirmar") | timeAppellation.equals("... sense confirmar") ) {
      timeUnconfirmed = true;
      return;
    }
    timeAppellation = timeAppellation.replaceAll("\\s+", " ");
    timeAppellation = timeAppellation.replace("?", "");
    timeAppellation = timeAppellation.replace("ca.", "");
    timeAppellation = timeAppellation.replace("circa", "");
    timeAppellation = timeAppellation.replace("early", "");
    timeAppellation = timeAppellation.replace("late", "");
    timeAppellation = timeAppellation.replace("mid-", "");
    timeAppellation = timeAppellation.replace("Finales ", "");
    timeAppellation = timeAppellation.replace("Primer tercio del ", "");
    timeAppellation = timeAppellation.replace("Principios ", "");
    timeAppellation = timeAppellation.replace("Primer tercio ", "");
    timeAppellation = timeAppellation.replace("about ", "");
    timeAppellation = timeAppellation.replaceAll("\\((.+)\\)", "");
    timeAppellation = timeAppellation.trim();
    Resource result = VocabularyManager.searchInCategory(timeAppellation, null, "dates", false);
    if (result != null) {
      //System.out.println(result.getURI());
      this.addProperty(CIDOC.P4_has_time_span, result); }
    else {


      TimeSpan ts = new TimeSpan(timeAppellation);
      //ts.addAppellation(timeAppellation);
      if (timeUnconfirmed) ts.addNote("unconfirmed", "en");

      this.addTimeSpan(ts);
    }
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
          collection = collection_level1.getURI();  }
        else {
          collection = collection_level2.getURI(); }
        if (collection.contains("materials") || collection.contains("300264091")) {
          this.addProperty(CIDOC.P126_employed, result);
        }
        else if (collection.contains("techniques") || collection.contains("300264090")) {
          this.addProperty(CIDOC.P32_used_general_technique, result);

        }
         else {
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
  //public void addPlace(String place) {
    this.addProperty(CIDOC.P8_took_place_on_or_within, place);
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
          collection = collection_level1.getURI();  }
          else {
            collection = collection_level2.getURI(); }
            if (collection.contains("techniques") || collection.contains("300264090")) {
              this.addProperty(CIDOC.P32_used_general_technique, result);

            }
            else if (collection.contains("materials") || collection.contains("300264091")) {
              this.addProperty(CIDOC.P126_employed, result);
            }
            else {
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

}
