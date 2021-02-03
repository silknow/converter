package org.silknow.converter.entities;

import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;

import java.util.regex.Pattern;


public class Modification extends Entity {




  public Modification(String id, String author, String year, String comment) {
    super(id);
    this.setClass(CIDOC.E11_Modification);
    this.setUri(ConstructURI.build(this.className, author));
    this.addNote(comment);
    this.addNote(author);
    TimeSpan ts = new TimeSpan(year);
    this.addTimeSpan(ts);

  }

  public Modification(String id, String author, String year) {
    super(id);
    this.setClass(CIDOC.E11_Modification);
    this.setUri(ConstructURI.build(this.className, year));

    this.addNote(author);
    TimeSpan ts = new TimeSpan(year);
    this.addTimeSpan(ts);

  }

  public Modification(String id, String comment) {
    super(id);
    this.setClass(CIDOC.E11_Modification);
    this.setUri(ConstructURI.build(this.className, comment));
    this.addNote(comment);

  }

  public void of(ManMade_Object obj) {
    this.addProperty(CIDOC.P31_has_modified, obj);
  }
}
