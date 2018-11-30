package org.silknow.converter.entities;

import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.CrawledJSONImages;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class Image extends Entity {
  public Image(String id) {
    super(id);

    this.setClass(CIDOC.E38_Image);
    this.addSimpleIdentifier(id);
  }

  public Image() {
    super();
    this.resource = model.createResource();
    this.setClass(CIDOC.E38_Image);
  }

  public static Image fromCrawledJSON(@NotNull CrawledJSONImages img) {
    Image image;
    if (img.hasId()) image = new Image(img.getId());
    else image = new Image();

    image.addProperty(Schema.contentUrl, ResourceFactory.createResource(img.getUrl()));
    return image;
  }
}
