package org.silknow.converter.entities;

import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.commons.CrawledJSONImages;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class Image extends Entity {
  private final static String MEDIA_BASE = "https://silknow.org/silknow/media/";
  private CrawledJSONImages sourceImg;
  private String localFilename;
  private boolean hasError;

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
    String seed = id + "$$$" + imgCount + this.localFilename; // the $$$ is there for avoiding collision of different-sized seeds
    //String seed = MEDIA_BASE;
    this.setUri(ConstructURI.build(this.source, "Image", seed));
  }

  public void setContentUrl(String url) {
    if (url == null || url.isEmpty()) return;
    this.resource.removeAll(Schema.contentUrl);
    this.addProperty(Schema.contentUrl, model.createResource(url));
  }

  public void setHasError(Boolean hasError) {
    this.hasError = hasError;
    if (hasError == false)
    return;
    if (hasError & this.localFilename != null)
      System.out.println("Local image could not be retrieved:" + this.localFilename);
  }

  public void addInternalUrl(String dataset) {
    if (this.hasError == false) {
      String filename = null;
      if (this.localFilename != null)
        filename = this.localFilename;
      else if (this.sourceImg != null)
        filename = this.sourceImg.getlocalFilename();
      if (filename == null)
        filename = this.getContentUrl().substring(this.getContentUrl().lastIndexOf('/') + 1);

      if (filename.trim().isEmpty()) return; // workaround for issue #38

      filename = filename.replaceAll(" ", "%20");
      String internalUrl = MEDIA_BASE + dataset + "/" + filename;

      this.addProperty(Schema.contentUrl, model.createResource(internalUrl));
      String seed = internalUrl;
      this.setUri(ConstructURI.build(this.source, "Image", seed));
    }

  }


  public static Image fromCrawledJSON(@NotNull CrawledJSONImages img) {
    Image image;
    if (img.hasId()) image = new Image(img.getId());
    else image = new Image();

    image.sourceImg = img;
    image.setContentUrl(img.getUrl());
    image.setHasError(img.gethasError());
    return image;
  }

  public String getContentUrl() {
    return this.resource.getProperty(Schema.contentUrl).getObject().toString();
  }

  public boolean hasError() {
    return hasError;
  }


  public void setLocalFilename(String name) {
    this.localFilename = name;
  }
}
