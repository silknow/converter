package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Condition extends Entity {
  public Condition(String uri) {
    super();
    this.setUri(uri);
    this.setClass(CIDOC.E3_Condition_State);
  }

}
