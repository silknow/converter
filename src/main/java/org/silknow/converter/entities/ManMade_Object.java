package org.silknow.converter.entities;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.ontologies.CIDOC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManMade_Object extends Entity {
  private static final String DIMENSION_REGEX = "(\\d+(?:[,.]\\d)?) x (\\d+(?:[,.]\\d)?)";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX);

  private int typeAssignmentCount;
  private int imgCount;

  public ManMade_Object(String id) {
    super(id);
    this.setClass(CIDOC.E22_Man_Made_Object);

    this.addSimpleIdentifier(id);
    typeAssignmentCount = 0;
    imgCount = 0;
  }

  public void addSubject(String subject) {
    this.addProperty(CIDOC.P62_depicts, subject);
  }

  public void addClassification(String classification, String type) {
    addClassification(classification, type, null);
  }


  public void addClassification(String classification, String type, LegalBody museum) {
    if (classification == null) return;
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
    if (img.hasNullUri()) img.setUri(this.getUri() + "/image/" + ++imgCount);
    this.addProperty(CIDOC.P138i_has_representation, img);
    return this;
  }

  public ManMade_Object add(Inscription ins) {
    this.addProperty(CIDOC.P128_carries, ins);
    return this;
  }

  public ManMade_Object add(Right right) {
    this.addProperty(CIDOC.P104_is_subject_to, right);
    return this;
  }

  public void associate(String npa) {
    if (npa == null) return;
    //    E22_Man Made Object P69 is associated with E39_Actor P1 is identified by E83_Actor Appellation
    this.addProperty(CIDOC.P69_has_association_with, new Actor(npa));
  }

  public void addMeasure(String value) throws RuntimeException {
    Matcher m = DIMENSION_PATTERN.matcher(value);
    if (!m.find()) throw new RuntimeException("Dimension not parsed: " + value);
    addMeasure(m.group(1), m.group(2));
  }

  public void addMeasure(String width, String height) {
    String dimUri = this.getUri() + "/dimension/";

    width = width.replace(",", ".");
    height = height.replace(",", ".");

    Dimension w = new Dimension(dimUri + "w", width, "cm", "width");
    this.addProperty(CIDOC.P43_has_dimension, w);
    Dimension h = new Dimension(dimUri + "h", height, "cm", "height");
    this.addProperty(CIDOC.P43_has_dimension, h);

    model.createResource(dimUri + "measurement")
            .addProperty(RDF.type, CIDOC.E16_Measurement)
            .addProperty(CIDOC.P39_measured, this.asResource())
            .addProperty(CIDOC.P40_observed_dimension, w.asResource())
            .addProperty(CIDOC.P40_observed_dimension, h.asResource());

    this.model.add(w.model).add(h.model);
  }

  public void addTitle(String title) {
    this.addProperty(RDFS.label, title)
            .addProperty(CIDOC.P102_has_title, title);
  }

  public void addInfo(String section, String text, String lang) {
    Resource sec = model.createResource(this.getUri() + "/section/" + section)
            .addProperty(RDF.type, CIDOC.E53_Place)
            .addProperty(RDFS.label, section)
            .addProperty(CIDOC.P87_is_identified_by, section);

    this.addProperty(CIDOC.P59_has_section, sec);

    model.createResource(this.getUri() + "/section/" + section + "/info")
            .addProperty(RDF.type, CIDOC.E73_Information_Object)
            .addProperty(CIDOC.P129_is_about, sec)
            .addProperty(CIDOC.P3_has_note, text, lang);
  }

}


