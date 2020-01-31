package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;

public class CrawledJSONImages {
  private String id;
  private String url;
  private String localFilename;
  private boolean hasError;

  public String getId() {
    return id;
  }

  public String getUrl() { return url; }

  public String getlocalFilename() { return localFilename; }

  public boolean gethasError() { return hasError; }

  public boolean hashasError() {
    return hasError;
  }

  public boolean hasId() {
    return id != null && !StringUtils.isBlank(id);
  }

}
