package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;

public class Actor extends Entity {
  public Actor(String name) {
    super();
    this.setUri(ConstructURI.build(this.className, name));

    this.setClass(CIDOC.E39_Actor);
    this.addProperty(RDFS.label, name)
            .addProperty(CIDOC.P1_is_identified_by, name);
  }
}
