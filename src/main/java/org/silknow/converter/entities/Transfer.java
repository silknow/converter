package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Transfer extends Entity {
  public Transfer(String id, String source) {
    super(id, source);
    this.setClass(CIDOC.E10_Transfer_of_Custody);
  }

  public Transfer of(ManMade_Object obj) {
    this.addProperty(CIDOC.P30_transferred_custody_of, obj);
    return this;
  }

  public Transfer by(LegalBody museum) {
    this.addProperty(CIDOC.P29_custody_received_by, museum);
    return this;
  }
}
