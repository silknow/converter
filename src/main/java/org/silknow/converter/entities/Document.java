package org.silknow.converter.entities;

import org.apache.jena.vocabulary.DC;
import org.silknow.converter.ontologies.CIDOC;

public class Document extends Entity {

  public Document(String id) {
    super(id);

    this.setClass(CIDOC.E31_Document);
    this.addSimpleIdentifier(id);
  }


  public void setSource(String source) {
    this.addProperty(DC.source, model.createLiteral(source));
  }

  public Document document(Entity entity) {
    this.addProperty(CIDOC.P70_documents, entity);
    return this;
  }

  public void addEditor(Actor actor) {
    this.addProperty(CIDOC.P105_right_held_by, actor);
  }

}
