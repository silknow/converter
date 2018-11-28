package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;

public class LegalBody extends Entity {
  public LegalBody(String name) {
    super();
    this.setUri(ConstructURI.build(this.className, name));

    this.setClass(CIDOC.E40_Legal_Body);
    this.addProperty(RDFS.label, name)
            .addProperty(CIDOC.P1_is_identified_by, name);

  }
}
