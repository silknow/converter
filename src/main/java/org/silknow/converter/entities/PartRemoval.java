package org.silknow.converter.entities;

import org.silknow.converter.commons.StopWordException;
import org.silknow.converter.entities.Entity;
import org.silknow.converter.ontologies.CIDOC;

public class PartRemoval extends Entity {
  public PartRemoval(String PartRemoval) {
    super(PartRemoval);
    this.setClass(CIDOC.E80_Part_Removal);
    this.addNote(PartRemoval);
  }

  public void diminished(ManMade_Object obj) {
    this.addProperty(CIDOC.P112_diminished, obj);
  }
  public void removed(ManMade_Object obj) { this.addProperty(CIDOC.P113_removed, obj);
  }


}
