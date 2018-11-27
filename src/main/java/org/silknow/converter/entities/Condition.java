package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Condition extends Entity {
  private final ConditionAssestment assestment;

  public Condition(String id, String source, String description) {
    super(id, source);
    this.setClass(CIDOC.E3_Condition_State);

    this.assestment = new ConditionAssestment(this.getUri() + "/assestment");
    this.assestment.add(this);

    this.addNote(description);
  }

  public void assestedBy(String actor) {
    this.assestment.addProperty(CIDOC.P14_carried_out_by, actor);
    this.model.add(this.assestment.model);
  }

  public void concerns(ManMade_Object obj) {
    this.assestment.addProperty(CIDOC.P34_concerned, obj);
    this.model.add(this.assestment.model);
  }
}
