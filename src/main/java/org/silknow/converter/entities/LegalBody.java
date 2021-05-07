package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDF;
import org.silknow.converter.ontologies.CIDOC;

public class LegalBody extends Actor {
  public LegalBody(String name) {
    super(name);
    this.setClass(CIDOC.E40_Legal_Body);
    this.addProperty(RDF.type, CIDOC.E39_Actor);
  }
}
