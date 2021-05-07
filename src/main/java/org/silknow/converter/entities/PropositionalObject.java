package org.silknow.converter.entities;

import org.apache.jena.rdf.model.Resource;
import org.silknow.converter.ontologies.CIDOC;

public class PropositionalObject extends Entity {
  public PropositionalObject(String id) {
    super(id);
    this.setClass(CIDOC.E89_Propositional_Object);
  }

  public PropositionalObject() {
    super();
    this.setClass(CIDOC.E89_Propositional_Object);
  }

  public PropositionalObject isAbout(ManMade_Object obj) {
    this.addProperty(CIDOC.P129_is_about, obj);
    return this;
  }

  public PropositionalObject setType(String type, String lang) {
    this.addProperty(CIDOC.P2_has_type, type, lang);
    return this;
  }

  public PropositionalObject setType(String type) {
    this.addProperty(CIDOC.P2_has_type, type);
    return this;
  }

  public PropositionalObject refersTo(ManMade_Object object) {
    this.addProperty(CIDOC.P67_refers_to, object);
    return this;
  }

  public PropositionalObject refersTo(Resource object) {
    this.addProperty(CIDOC.P67_refers_to, object);
    return this;
  }
}
