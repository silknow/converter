package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class ConditionAssestment extends Entity {
  public ConditionAssestment(String uri) {
    super();
    this.setUri(uri);
    this.setClass(CIDOC.E14_Condition_Assessment);

  }

  public void add(Condition condition) {
    this.addProperty(CIDOC.P35_has_identified, condition);
  }
}
