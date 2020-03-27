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
    Resource result = VocabularyManager.searchInCategory(timeAppellation, null, "dates", false);
    if (result != null) {
      //System.out.println(result.getURI());
      this.addProperty(CIDOC.P4_has_time_span, result); }
    else {


      TimeSpan ts = new TimeSpan();
      ts.addAppellation(timeAppellation);
      if (timeUnconfirmed) ts.addNote("unconfirmed", "en");

      ts.setUri(this.getUri() + "/time/" + ++tsCount);
      this.addTimeSpan(ts);
    }
  }

  public Production add(ManMade_Object obj) {
    this.addProperty(CIDOC.P108_has_produced, obj);
    return this;
  }

  public void addMaterial(String material, String lang) {
    Resource result = VocabularyManager.searchInCategory(material, null, "aat", false);
    if (result != null) {
      ResIterator resIterator = result.getModel().listResourcesWithProperty(SKOS.member, result);
      while (resIterator.hasNext()) {
        String collection = resIterator.next().getURI();
        if (collection.contains("animal_fibres") || collection.contains("gold_thread") || collection.contains("metal_thread")
            || collection.contains("metals") || collection.contains("mixed_fibre") || collection.contains("other_material") || collection.contains("paper")
                || collection.contains("silver_thread") || collection.contains("synthtetic_fibres") || collection.contains("vegetal_fibres")
                || collection.contains("300264091")) {
          //System.out.println(collection);
          this.addProperty(CIDOC.P126_employed, result);
          break;

        }
        if (collection.contains("brocatelle") || collection.contains("cannele") || collection.contains("damask")
                || collection.contains("damasse") || collection.contains("effect") || collection.contains("embroidery") || collection.contains("gauze")
                || collection.contains("lampas") || collection.contains("metal_effect") || collection.contains("other_technique")
                || collection.contains("patterned_fabric") || collection.contains("printed_fabric") || collection.contains("rippled_effect")
                || collection.contains("satin") || collection.contains("tabby") || collection.contains("twill")
                || collection.contains("velvet") || collection.contains("300264090")) {
          //System.out.println(collection);
          this.addProperty(CIDOC.P32_used_general_technique, result);
          break;
        }
        else {
          result = null;
          break;
        }
      }
    };
     if (result == null) {
//      System.out.println("Material not found in vocabularies: " + material);
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
  }

  public void addTechnique(String technique, String lang) {
    Resource result = VocabularyManager.searchInCategory(technique, null, "aat", false);
    if (result != null) {
      ResIterator resIterator = result.getModel().listResourcesWithProperty(SKOS.member, result);
      while (resIterator.hasNext()) {
        String collection = resIterator.next().getURI();
        if (collection.contains("animal_fibres") || collection.contains("gold_thread") || collection.contains("metal_thread")
                || collection.contains("metals") || collection.contains("mixed_fibre") || collection.contains("other_material") || collection.contains("paper")
                || collection.contains("silver_thread") || collection.contains("synthtetic_fibres") || collection.contains("vegetal_fibres")
                || collection.contains("300264091")) {
          //System.out.println(collection);
          this.addProperty(CIDOC.P126_employed, result);
          break;

        }
        if (collection.contains("brocatelle") || collection.contains("cannele") || collection.contains("damask")
                || collection.contains("damasse") || collection.contains("effect") || collection.contains("embroidery") || collection.contains("gauze")
                || collection.contains("lampas") || collection.contains("metal_effect") || collection.contains("other_technique")
                || collection.contains("patterned_fabric") || collection.contains("printed_fabric") || collection.contains("rippled_effect")
                || collection.contains("satin") || collection.contains("tabby") || collection.contains("twill")
                || collection.contains("velvet")  || collection.contains("300264090") ) {
          //System.out.println(collection);
          this.addProperty(CIDOC.P32_used_general_technique, result);
          break;
        }
        else {
          result = null;
          break;
        }
      }
    };
    if (result == null) {
//      System.out.println("Material not found in vocabularies: " + technique);
      this.addProperty(CIDOC.P32_used_general_technique, technique, lang);
    }
  }

  public void addTool(ManMade_Object tool) {
    this.addProperty(CIDOC.P16_used_specific_object, tool);
  }
}
