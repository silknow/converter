package org.silknow.converter.entities;

import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.CrawledJSONImages;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class Image extends Entity {
  private final static String MEDIA_BASE = "http://silknow.org/silknow/media/";
  private CrawledJSONImages sourceImg;

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

  public void addInternalUrl(String dataset) {
    String filename =null;
    if (this.sourceImg != null)
      filename = this.sourceImg.getlocalFilename();
    if(filename == null)
      filename = this.getContentUrl().substring(this.getContentUrl().lastIndexOf('/') + 1);

    if (filename.trim().isEmpty()) return; // workaround for issue #38

    filename = filename.replaceAll("\\s+", "_");
    String internalUrl = MEDIA_BASE + dataset + "/" + filename;
    this.addProperty(Schema.contentUrl, model.createResource(internalUrl));
  }

  public static Image fromCrawledJSON(@NotNull CrawledJSONImages img) {
    Image image;
    if (img.hasId()) image = new Image(img.getId());
    else image = new Image();

    image.sourceImg = img;
    image.setContentUrl(img.getUrl());
    return image;
  }

  public String getContentUrl() {
    return this.resource.getProperty(Schema.contentUrl).getObject().toString();
  }

}
