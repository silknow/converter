package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Dimension extends Entity {
  public Dimension(String uri, String value, String unit) {
    super();
    this.setUri(uri);
    this.setClass(CIDOC.E54_Dimension);
    this.addProperty(CIDOC.P90_has_value, value);
    this.addProperty(CIDOC.P91_has_unit, unit);
  }
}
