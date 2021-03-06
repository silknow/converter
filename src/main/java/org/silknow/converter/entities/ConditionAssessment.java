package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

import java.util.ArrayList;
import java.util.List;

public class ConditionAssessment extends Entity {
  List<Condition> conditions;

  public ConditionAssessment(String id) {
    super(id);
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
    int Counter = conditions.size() + 1;
    Condition condition = new Condition(this.getUri() + "/condition/" + Counter);

    condition.addType(type);
    condition.addNote(value, lang);
    this.add(condition);
  }

  public List<Condition> getConditions() {
    return conditions;
  }
}
