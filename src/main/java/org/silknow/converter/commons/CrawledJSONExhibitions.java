package org.silknow.converter.commons;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CrawledJSONExhibitions {
  private String url;
  private String title;
  private String date;
  private String blurb;

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public String getDate() {
    return date;
  }

  public String getBlurb() {
    return blurb;
  }

  public boolean hasTitle() {
    return title != null && !StringUtils.isBlank(title);
  }
}
