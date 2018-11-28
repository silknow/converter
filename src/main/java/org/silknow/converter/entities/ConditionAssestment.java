package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

import java.util.ArrayList;
import java.util.List;

public class ConditionAssestment extends Entity {
  List<Condition> conditions;

  public ConditionAssestment(String id, String source) {
    super(id, source);
    this.setClass(CIDOC.E14_Condition_Assessment);
    conditions = new ArrayList<>();
  }

  public void assestedBy(LegalBody actor) {
    this.addProperty(CIDOC.P14_carried_out_by, actor);
  }

  public void concerns(ManMade_Object obj) {
    this.addProperty(CIDOC.P34_concerned, obj);
  }

  public void add(Condition condition) {
    conditions.add(condition);
    this.addProperty(CIDOC.P35_has_identified, condition);
  }

  public void addCondition(String type, String value, String lang) {
    Condition condition = new Condition(this.getUri() + "/condition/" + conditions.size() + 1);
    condition.addType(type);
    condition.addNote(value, lang);
    this.add(condition);
  }

  public List<Condition> getConditions() {
    return conditions;
  }
}
