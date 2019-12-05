package org.silknow.converter.entities;

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

  public void setContentUrl(String url) {
    this.resource.removeAll(Schema.contentUrl);
    this.addProperty(Schema.contentUrl, model.createResource(url));
  }

  public void setCERorMTMADUrl(String url) {
    this.addProperty(Schema.contentUrl, model.createResource(url));
  }

  public static Image fromCrawledJSON(@NotNull CrawledJSONImages img) {
    Image image;
    if (img.hasId()) image = new Image(img.getId());
    else image = new Image();


    image.setContentUrl(img.getUrl());
    return image;
  }

  public static Image CERfromCrawledJSON(@NotNull CrawledJSONImages img) {
    Image image;
    if (img.hasId()) image = new Image(img.getId());
    else image = new Image();


    image.setContentUrl(img.getUrl());
    image.setCERorMTMADUrl("http://silknow.org/silknow/media/ceres-mcu/" + img.getlocalFilename());

    return image;
  }



  public static Image MTMADfromCrawledJSON(@NotNull CrawledJSONImages img) {
    Image image;
    if (img.hasId()) image = new Image(img.getId());
    else image = new Image();


    image.setContentUrl(img.getUrl());
    image.setCERorMTMADUrl("http://silknow.org/silknow/media/mtmad/" + img.getlocalFilename());

    return image;
  }


  public String getContentUrl() {
    return this.resource.getProperty(Schema.contentUrl).getObject().toString();
  }



}
