package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Production extends Entity {
  private int tsCount;
  private boolean timeUnconfirmed;

  public Production(String id, String source) {
    super(id, source);
    this.setClass(CIDOC.E12_Production);

    tsCount = 0;
    timeUnconfirmed = false;
  }

  public void addTimeAppellation(String timeAppellation) {
    if (timeAppellation.equals("... unconfirmed")) {
      timeUnconfirmed = true;
      return;
    }

    TimeSpan ts = new TimeSpan();
    ts.addAppellation(timeAppellation);
    if (timeUnconfirmed) ts.addNote("unconfirmed");

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
    if (place.equals("... unconfirmed"))
      // TODO something?
      return;

    this.addProperty(CIDOC.P8_took_place_on_or_within, place);
  }
}
