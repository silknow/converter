package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.ontologies.CIDOC;

public class TimeSpan extends Entity {
  public TimeSpan() {
    super();
    createResource();
    this.setClass(CIDOC.E52_Time_Span);
  }

  public void addAppellation(String timeAppellation) {
    this.addProperty(RDFS.label, timeAppellation)
            .addProperty(CIDOC.P78_is_identified_by, timeAppellation);
  }

}
