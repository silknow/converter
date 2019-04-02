package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
    return id != null && !StringUtils.isBlank(id);
  }
}
