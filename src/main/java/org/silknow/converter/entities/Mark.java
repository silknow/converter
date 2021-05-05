package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Mark extends Entity {
  public Mark(String Mark) {
    super(Mark);
    this.setClass(CIDOC.E37_Mark);
    this.addNote(Mark);
  }

  public void carries(ManMade_Object obj) {
    this.addProperty(CIDOC.P128_carries, obj);
  }


}
