package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Silknow;

public class Pattern_Unit extends Entity {



  public Pattern_Unit(String uri, String length, String unit) {
    super();
    this.setUri(uri);
    this.setClass(Silknow.T24);
    Dimension l = new Dimension(this.getUri(), length, unit);
    this.addProperty(CIDOC.P43_has_dimension, l);

  }


}
