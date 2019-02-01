package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDFS;
import org.geonames.Toponym;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.commons.GeoNames;
import org.silknow.converter.ontologies.CIDOC;

public class Place extends Entity {
  public Place(String name) {
    super();

    Toponym tp = GeoNames.query(name);
    if (tp != null) {
      GeoNames.downloadRdf(tp.getGeoNameId());
      this.setUri(GeoNames.toURI(tp.getGeoNameId()));
    } else
      this.setUri(ConstructURI.build(this.className, name));

    this.setClass(CIDOC.E53_Place);
    this.addProperty(RDFS.label, name).addProperty(CIDOC.P1_is_identified_by, name);
  }
}
