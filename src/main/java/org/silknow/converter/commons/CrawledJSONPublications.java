package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CrawledJSONPublications {
  private String url;
  private String title;
  private String subtitle;

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public boolean hasTitle() {
    return title != null && !StringUtils.isBlank(title);
  }
}
