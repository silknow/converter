package org.silknow.converter.entities;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.silknow.converter.ontologies.CIDOC;

public class Dimension extends Entity {
  public Dimension(String uri, String value, String unit) {
    super();
    this.setUri(uri);
    this.setClass(CIDOC.E54_Dimension);
    this.addProperty(CIDOC.P90_has_value, value, XSDDatatype.XSDfloat);
    this.addProperty(CIDOC.P91_has_unit, unit);
  }

  public Dimension(String uri, String value, String unit, String type) {
    this(uri, value, unit);
    this.addProperty(CIDOC.P2_has_type, type);
  }
}
