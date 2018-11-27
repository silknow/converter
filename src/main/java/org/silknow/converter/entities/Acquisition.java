package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Acquisition extends Entity {
  public Acquisition(String id, String source) {
    super(id, source);
    this.setClass(CIDOC.E8_Acquisition);
  }

  public void transfer(String from, ManMade_Object of, String to) {
    this.addProperty(CIDOC.P23_transferred_title_from, from)
            .addProperty(CIDOC.P24_transferred_title_of, of)
            .addProperty(CIDOC.P22_transferred_title_to, to);
  }

  public void setType(String type) {
    this.addProperty(CIDOC.P2_has_type, type);
  }
}
