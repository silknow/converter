package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CrawledJSONImages {
  private String id;
  private String url;
  private String localFilename;

  public String getId() {
    return id;
  }

  public String getUrl() { return url; }

  public String getlocalFilename() { return localFilename; }


  public boolean hasId() {
    return id != null && !StringUtils.isBlank(id);
  }

  public boolean hasFilename() {
    return id != null && !StringUtils.isBlank(localFilename);
  }

}
