package org.silknow.converter.commons;

public class CrawledJSONImages {
  private String id;
  private String url;

  public String getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public boolean hasId() {
    return id != null && !id.isBlank();
  }
}
