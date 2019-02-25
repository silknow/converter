package org.silknow.converter.entities;

import org.silknow.converter.commons.StopWordException;
import org.silknow.converter.ontologies.CIDOC;

public class Move extends Entity {
  public Move(String id) {
    super(id);
    this.setClass(CIDOC.E9_Move);
  }

  public Move from(String place) {
    try {
      this.addProperty(CIDOC.P27_moved_from, new Place(place));
    } catch (StopWordException e) {
      e.printStackTrace();
    }
    return this;
  }

  public Move to(String place) {
    try {
      this.addProperty(CIDOC.P26_moved_to, new Place(place));
    } catch (StopWordException e) {
      e.printStackTrace();
    }
    return this;

  }

  public Move of(ManMade_Object obj) {
    this.addProperty(CIDOC.P25_moved, obj);
    return this;
  }
}
