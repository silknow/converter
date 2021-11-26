package org.silknow.converter.entities;

import org.apache.jena.rdf.model.Resource;
import org.doremus.string2vocabulary.VocabularyManager;
import org.silknow.converter.ontologies.CIDOC;

public class Activity extends Entity {

  public Activity(String uri) {
    super();
    this.setUri(uri);
    this.setClass(CIDOC.E7_Activity);
  }

  public Activity(String id, String flag) {
    super(id + flag);
    this.setClass(CIDOC.E7_Activity);
  }

  public void addActor(Actor actor) {
    this.addProperty(CIDOC.P14_carried_out_by, actor);
  }

  public void addActor(String actor) {
    this.addActor(new Actor(actor));
  }


}
