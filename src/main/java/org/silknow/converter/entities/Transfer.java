package org.silknow.converter.entities;

import org.silknow.converter.commons.StopWordException;
import org.silknow.converter.ontologies.CIDOC;

public class Transfer extends Entity {
  public Transfer(String id) {
    super(id);
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

  public Transfer by(String storage) {
    try {
    this.addProperty(CIDOC.P29_custody_received_by, new Place(storage));
    } catch (StopWordException e) {
      e.printStackTrace(); }
    return this;
  }
}
