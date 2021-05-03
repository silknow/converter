package org.silknow.converter.entities;

import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;

public class Visual_Item extends Entity {



  public Visual_Item(String note, String lang) {
    super();
    String seed = note;
    this.setUri(ConstructURI.build(this.source, "Image", seed));
    this.setClass(CIDOC.E36_Visual_Item);
    this.addNote(note, lang);

  }


}
