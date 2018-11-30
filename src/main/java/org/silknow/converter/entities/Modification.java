package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Modification extends Entity {
  public Modification(String id, String type, String description) {
    super(id);
    this.setClass(CIDOC.E11_Modification);
    this.addType(type);
    this.addNote(description);
  }

  public void of(ManMade_Object obj) {
    this.addProperty(CIDOC.P31_has_modified, obj);
  }
}
