package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Image extends Entity {
  public Image(String id, String source) {
    super(id, source);

    this.setClass(CIDOC.E38_Image);
    this.addSimpleIdentifier(id);
  }
}
