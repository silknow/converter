package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;

public class Collection extends Entity {
  public Collection(String dataset, String id) {
    super(id);
    this.setUri(ConstructURI.build(dataset, this.className, id));
    this.setClass(CIDOC.E78_Collection);
  }

  public void of(ManMade_Object obj) {
    this.addProperty(CIDOC.P106_is_composed_of , obj);
  }

  public void addAppellation(String timeAppellation) {
    this.addProperty(RDFS.label, timeAppellation)
            .addProperty(CIDOC.P78_is_identified_by, timeAppellation);
  }


}

