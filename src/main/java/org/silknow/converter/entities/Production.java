package org.silknow.converter.entities;

import org.apache.jena.rdf.model.Resource;
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
    if (timeAppellation.equals("... unconfirmed")) {
      timeUnconfirmed = true;
      return;
    }

    TimeSpan ts = new TimeSpan();
    ts.addAppellation(timeAppellation);
    if (timeUnconfirmed) ts.addNote("unconfirmed", "en");

    ts.setUri(this.getUri() + "/time/" + ++tsCount);
    this.addTimeSpan(ts);
  }

  public Production add(ManMade_Object obj) {
    this.addProperty(CIDOC.P108_has_produced, obj);
    return this;
  }

  public void addMaterial(String material) {
    this.addProperty(CIDOC.P126_employed, material);
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

  public void addTechnique(String technique) {
    this.addProperty(CIDOC.P32_used_general_technique, technique);
  }

  public void addTool(ManMade_Object tool) {
    this.addProperty(CIDOC.P16_used_specific_object, tool);
  }
}
