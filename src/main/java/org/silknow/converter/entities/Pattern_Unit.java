package org.silknow.converter.entities;

import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.SILKNOW;

public class Pattern_Unit extends Entity {



  public Pattern_Unit(String uri, String length, String unit) {
    super();
    this.setUri(uri);
    this.setClass(SILKNOW.T24_Pattern_Unit);
    Dimension l = new Dimension(this.getUri() + "dimension/", length, unit);
    this.addProperty(CIDOC.P43_has_dimension, l);

  }


}
