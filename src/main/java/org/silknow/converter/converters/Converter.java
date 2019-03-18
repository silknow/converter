package org.silknow.converter.converters;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.entities.Entity;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.CRMdig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class Converter {
  protected static final String BASE_URI = "http://data.silknow.org/";
  protected final Model model = ModelFactory.createDefaultModel();
  protected String DATASET_NAME;


  Logger logger = LoggerFactory.getLogger(getClass());
  protected String id; // record id
  private Resource dataset;
  private Resource record;

  public abstract boolean canConvert(File file);

  public abstract Model convert(File file);

  protected boolean isJson(File file) {
    return file.getName().endsWith(".json");
  }

  protected boolean isExcel(File file) {
    return file.getName().endsWith(".xls");
  }

  private void linkToDataset(Resource record) {
    if (this.dataset == null) {
      this.dataset = model.createResource(BASE_URI + this.DATASET_NAME)
              .addProperty(RDF.type, CRMdig.D1_Digital_Object)
              .addProperty(RDFS.label, this.DATASET_NAME)
              .addProperty(CIDOC.P2_has_type, "dataset");
    }
    this.dataset.addProperty(CIDOC.P106_is_composed_of, record);
  }


  protected void linkToRecord(Resource any) {
    if (any == null) return;
    if (this.record == null) {
      String recordUri = BASE_URI + this.DATASET_NAME + "/" + id.replaceAll("\\s", "_");
      this.record = model.createResource(recordUri)
              .addProperty(RDF.type, CRMdig.D1_Digital_Object)
              .addProperty(RDFS.label, id)
              .addProperty(CIDOC.P2_has_type, "record");
      linkToDataset(record);
    }
    this.record.addProperty(CIDOC.P129_is_about, any);
  }

  protected void linkToRecord(Entity obj) {
    this.linkToRecord(obj.asResource());
    this.model.add(obj.getModel());
  }

  public void resetModel() {
    this.record = null;
    model.removeAll();
  }
}
