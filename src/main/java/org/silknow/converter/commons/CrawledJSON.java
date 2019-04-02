package org.silknow.converter.commons;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Stream;

/*
 * Class for mapping the JSON coming from the crawler
 * https://github.com/silknow/crawler
 */
public class CrawledJSON {
  private List<CrawledJSONField> fields;
  private List<CrawledJSONImages> images;
  private String multiSeparator;

  public static CrawledJSON from(File file) throws FileNotFoundException {
    return new Gson().fromJson(new FileReader(file), CrawledJSON.class);
  }

  private CrawledJSONField getField(String label) {
    return fields.stream()
            .filter(f -> f.hasLabel(label))
            .filter(CrawledJSONField::isNotNull)
            .findFirst().orElse(null);
  }

  public String get(String label) {
    CrawledJSONField f = getField(label);
    if (f == null) return null;
    return f.getValue();
  }

  public Stream<String> getMulti(String label) {
    return getMulti(label, this.multiSeparator);
  }

  public Stream<String> getMulti(String label, String separator) {
    CrawledJSONField f = getField(label);
    if (f == null) return Stream.empty();
    return f.getMultiValue(separator);
  }


  public void setMultiSeparator(String separator) {
    this.multiSeparator = separator;
  }

  public Stream<CrawledJSONImages> getImages() { return this.images.stream(); }
}
