package org.silknow.converter.commons;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class Converter {
  protected Logger logger = LoggerFactory.getLogger(getClass());

  public abstract boolean canConvert(File file);

  public abstract Model convert(File file);

  protected boolean isJson(File file) {
    return file.getName().endsWith(".json");
  }
}
