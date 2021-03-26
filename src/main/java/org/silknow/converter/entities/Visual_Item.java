package org.silknow.converter.entities;

import jdk.internal.loader.Resource;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class Visual_Item extends Entity {



  public Visual_Item(String note, String lang) {
    super();
    String seed = note;
    this.setUri(ConstructURI.build(this.className, seed));
    this.setClass(CIDOC.E36_Visual_Item);
    this.addNote(note, lang);

  }


}
