package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Right extends Entity {
  public Right(String uri) {
    super();

    setUri(uri);
    this.setClass(CIDOC.E30_Right);
  }

  public void ownedBy(Actor owner) {
    this.addProperty(CIDOC.P75i_is_possessed_by, owner);
  }

  public void applyTo(Entity obj) {
    this.addProperty(CIDOC.P104i_applies_to, obj);
  }
}
