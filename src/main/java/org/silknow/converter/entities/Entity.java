package org.silknow.converter.entities;


import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.Contract;
import org.silknow.converter.Main;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.CRMsci;

public abstract class Entity {
  String className;
  protected String source;

  Model model;
  private String uri;
  protected Resource resource;
  protected String id;
  private int activityCount;
  private int observationCount;

  Entity() {
    // do nothing, enables customisation for child class
    this.model = ModelFactory.createDefaultModel();
    this.className = this.getClass().getSimpleName();

    this.activityCount = 0;
    this.observationCount = 0;
  }

  Entity(String id) {
    this();
    this.id = id;
    this.source = Main.source;

    /* create RDF resource */
    createResource();
  }

  void createResource() {
    this.resource = model.createResource(this.getUri());
  }

  @Contract(pure = true)
  boolean hasNullUri() {
    return this.uri == null;

  }

  public String getUri() {
    if (hasNullUri()) this.uri = ConstructURI.build(this.source, this.className, this.id);
    return this.uri;
  }

  protected void setUri(String uri) {
    if (this.uri != null && uri.equals(this.uri)) return;

    this.uri = uri;

    if (this.resource != null)
      this.resource = ResourceUtils.renameResource(this.resource, uri);
    else createResource();
  }

  //
//  public void addProvenance(Resource intermarcRes, Resource provActivity) {
//    this.asResource().addProperty(RDF.type, PROV.Entity)
//            .addProperty(PROV.wasAttributedTo, PP2RDF.DOREMUS)
//            .addProperty(PROV.wasDerivedFrom, intermarcRes)
//            .addProperty(PROV.wasGeneratedBy, provActivity);
//  }

  @Contract(pure = true)
  public Resource asResource() {
    return this.resource;
  }

  public Model getModel() {
    return this.model;
  }


  public void addNote(String text) {
    this.addNote(text, null);
  }

  public void addNote(String text, String lang) {
    this.addProperty(RDFS.comment, text, lang).addProperty(CIDOC.P3_has_note, text, lang);
  }

  public Resource addObservation(String text, String lang, String type) {
    if (StringUtils.isBlank(text)) return null;
    text = text.trim();
    this.addNote(text, lang);

    return model.createResource(this.uri + "/observation/" + ++observationCount)
            .addProperty(RDF.type, CRMsci.S4_Observation)
            .addProperty(CRMsci.O8_observed, this.asResource())
            .addProperty(CIDOC.P3_has_note, text, lang)
            .addProperty(CIDOC.P2_has_type, type);
  }


  protected void addType(String type) {
    this.addProperty(CIDOC.P2_has_type, type);
  }

  protected void setClass(OntClass _class) {
    this.resource.removeAll(RDF.type).addProperty(RDF.type, _class);
  }

  public Entity addProperty(Property property, Entity entity) {
    if (entity != null) {
      this.addProperty(property, entity.asResource());
      this.model.add(entity.getModel());
    }
    return this;
  }

  public Entity addProperty(Property property, Resource resource) {
    if (resource != null) this.resource.addProperty(property, resource);
    return this;
  }

  public Entity addProperty(Property property, String literal) {
    if (literal != null && !StringUtils.isBlank(literal)) this.resource.addProperty(property, literal.trim());
    return this;
  }

  public Entity addProperty(Property property, String literal, String lang) {
    if (literal != null && !StringUtils.isBlank(literal)) this.resource.addProperty(property, literal.trim(), lang);
    return this;
  }

  public Entity addProperty(Property property, Literal literal) {
    if (literal != null) this.resource.addProperty(property, literal);
    return this;
  }

  public Entity addProperty(Property property, String literal, XSDDatatype datatype) {
    if (literal != null && !StringUtils.isBlank(literal))
      this.resource.addProperty(property, literal.trim(), datatype);
    return this;
  }

  public void addTimeSpan(TimeSpan timeSpan) {
    if (timeSpan == null) return;
    if (timeSpan.hasNullUri()) timeSpan.setUri(this.uri + "/time");
    this.addProperty(CIDOC.P4_has_time_span, timeSpan);
  }

  public void addActivity(String actor, String function) {
    if (actor == null) return;
    this.addActivity(new Actor(actor), function);
  }

  public void addActivity(Actor actor, String function) {
    if (actor == null) return;

    Resource activity = model.createResource(this.uri + "/activity/" + ++activityCount)
            .addProperty(RDF.type, CIDOC.E7_Activity)
            .addProperty(CIDOC.P14_carried_out_by, actor.asResource());

    if (function != null) activity.addProperty(CIDOC.P2_has_type, function);

    this.addProperty(CIDOC.P9_consists_of, activity);
    this.model.add(actor.model);
  }

  protected void addSimpleIdentifier(String id) {
    this.addProperty(DC.identifier, id);
  }

  public void addComplexIdentifier(String id, String type, LegalBody issuer, Document doc) {
    this.addComplexIdentifier(id, type, issuer, doc, null);
  }

  public void addComplexIdentifier(String id, String type, LegalBody issuer, Document doc, String replaceId) {
    if (id == null) return;

    Resource identifier = model.createResource(this.uri + "/id/" + id.replaceAll(" ", "_"))
            .addProperty(RDF.type, CIDOC.E42_Identifier)
            .addProperty(RDFS.label, id)
            .addProperty(CIDOC.P2_has_type, type);

    Resource assignment = model.createResource(this.uri + "/id_assignment/" + id)
            .addProperty(RDF.type, CIDOC.E15_Identifier_Assignment)
            .addProperty(CIDOC.P37_assigned, identifier)
            .addProperty(CIDOC.P14_carried_out_by, issuer.asResource());

    if (replaceId != null) {
      Resource rIdentifier = model.createResource(this.uri + "/id/" + replaceId.replaceAll(" ", "_"))
              .addProperty(RDF.type, CIDOC.E42_Identifier)
              .addProperty(RDFS.label, replaceId)
              .addProperty(CIDOC.P2_has_type, "old register number");
      assignment.addProperty(CIDOC.P38_deassigned, rIdentifier);
    }

    doc.addProperty(CIDOC.P70_documents, assignment);
    this.addProperty(CIDOC.P1_is_identified_by, identifier);
    this.model.add(issuer.getModel());
  }


}
