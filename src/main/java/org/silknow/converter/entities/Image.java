package org.silknow.converter.entities;

import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.commons.CrawledJSONImages;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class Image extends Entity {
  private final static String MEDIA_BASE = "http://silknow.org/silknow/media/";
  private CrawledJSONImages sourceImg;
  private String localFilename;

  public Image(String id) {
    super(id);

    this.localFilename = null;
    this.setClass(CIDOC.E38_Image);
    this.addSimpleIdentifier(id);
  }

  public Image() {
    super();
    this.localFilename = null;
    this.resource = model.createResource();
    this.setClass(CIDOC.E38_Image);
  }

  public void generateUri(String id, int imgCount) {
    // this method is called if the img has no identifier and need one uri
    String seed = id + "$$$" + imgCount; // the $$$ is there for avoiding collision of different-sized seeds
    this.setUri(ConstructURI.build(this.source, "Image", seed));
  }

  public void setContentUrl(String url) {
    if (url == null || url.isEmpty()) return;
    this.resource.removeAll(Schema.contentUrl);
    this.addProperty(Schema.contentUrl, model.createResource(url));
  }

  public void addInternalUrl(String dataset) {
    String filename = null;
    if (this.localFilename != null)
      filename = this.localFilename;
    else if (this.sourceImg != null)
      filename = this.sourceImg.getlocalFilename();
    if (filename == null)
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

  public void setLocalFilename(String name) {
    this.localFilename = name;
  }
}
