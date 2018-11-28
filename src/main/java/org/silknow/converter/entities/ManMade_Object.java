package org.silknow.converter.entities;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.silknow.converter.ontologies.CIDOC;

public class ManMade_Object extends Entity {
  private int typeAssignmentCount;
  private Dimension dimension;

  public ManMade_Object(String id, String source) {
    super(id, source);
    this.setClass(CIDOC.E22_Man_Made_Object);

    this.addSimpleIdentifier(id);
    typeAssignmentCount = 0;
  }

  public Dimension getDimension() {
    return dimension;
  }

  public void addSubject(String subject) {
    this.addProperty(CIDOC.P62_depicts, subject);
  }

  public void addClassification(String classification, String type) {
    addClassification(classification, type, null);
  }


  public void addClassification(String classification, String type, LegalBody museum) {
    Resource assignment = model.createResource(this.getUri() + "/type_assignment/" + ++typeAssignmentCount)
            .addProperty(RDF.type, CIDOC.E17_Type_Assignment)
            .addProperty(CIDOC.P41_classified, this.asResource())
            .addProperty(CIDOC.P2_has_type, type)
            .addProperty(CIDOC.P42_assigned, classification);

    if (museum != null) {
      assignment.addProperty(CIDOC.P14_carried_out_by, museum.asResource());
      this.model.add(museum.getModel());
    }

    // this.addProperty(CIDOC.P2_has_type, classification);
  }

  public void addIntention(String intention) {
    this.addProperty(CIDOC.P103_was_intended_for, intention);
  }

  public ManMade_Object add(Image img) {
    img.addProperty(CIDOC.P138_represents, this);
    model.add(img.model);
    return this;
  }

  public void associate(String npa) {
    //    E22_Man Made Object P69 is associated with E39_Actor P1 is identified by E83_Actor Appellation
    this.addProperty(CIDOC.P69_has_association_with, npa);
  }

  public void addMeasure(String value) {
    String dimUri = this.getUri() + "/dimension";
    this.dimension = new Dimension(dimUri, value, "cm");

    this.addProperty(CIDOC.P43_has_dimension, dimension);

    model.createResource(dimUri + "/measurement")
            .addProperty(RDF.type, CIDOC.E16_Measurement)
            .addProperty(CIDOC.P39_measured, this.asResource())
            .addProperty(CIDOC.P40_observed_dimension, dimension.asResource());
  }
}


