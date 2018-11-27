package org.silknow.converter.entities;


import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.Contract;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;

public abstract class Entity {
  private String className;
  private String source;

  Model model;
  private String uri;
  private Resource resource;
  private String id;

  Entity() {
    // do nothing, enables customisation for child class
    this.model = ModelFactory.createDefaultModel();
    this.className = this.getClass().getSimpleName();
  }

  Entity(String id, String source) {
    this();
    this.id = id;
    this.source = source;

    /* create RDF resource */
    createResource();
  }

  void createResource() {
    this.resource = model.createResource(this.getUri());
  }

  @Contract(pure = true)
  boolean hasUri() {
    return this.uri != null;

  }

  protected String getUri() {
    if (!hasUri()) this.uri = ConstructURI.build(this.source, this.className, this.id);
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
  Resource asResource() {
    return this.resource;
  }

  public Model getModel() {
    return this.model;
  }


  protected void addNote(String text) {
    if (text == null || text.isBlank()) return;
    text = text.trim();
    this.addProperty(RDFS.comment, text).addProperty(CIDOC.P3_has_note, text);
  }

  protected void setClass(OntClass _class) {
    this.resource.addProperty(RDF.type, _class);
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
    if (literal != null && !literal.isBlank()) this.resource.addProperty(property, literal.trim());
    return this;
  }

  public Entity addProperty(Property property, String literal, String lang) {
    if (literal != null && !literal.isBlank()) this.resource.addProperty(property, literal.trim(), lang);
    return this;
  }

  public Entity addProperty(Property property, Literal literal) {
    if (literal != null) this.resource.addProperty(property, literal);
    return this;
  }

  public Entity addProperty(Property property, String literal, XSDDatatype datatype) {
    if (literal != null && !literal.isBlank()) this.resource.addProperty(property, literal.trim(), datatype);
    return this;
  }

  public void addTimeSpan(TimeSpan timeSpan) {
    if (timeSpan == null) return;
    if (!timeSpan.hasUri()) timeSpan.setUri(this.uri + "/time");
    this.addProperty(CIDOC.P4_has_time_span, timeSpan);
  }

//  public void addActivity(Artist agent, String function) {
//    if (agent == null) return;
//
//    String cacheId = agent.getFullName() + function;
//    if (activitiesCache.contains(cacheId)) return;
//
//    Resource activity = model.createResource(this.uri + "/activity/" + ++activityCount)
//            .addProperty(RDF.type, CIDOC.E7_Activity)
//            .addProperty(MUS.U31_had_function, function)
//            .addProperty(CIDOC.P14_carried_out_by, agent.asResource());
//
//    this.addProperty(CIDOC.P9_consists_of, activity);
//    this.model.add(agent.getModel());
//    activitiesCache.add(cacheId);
//  }

  protected void addSimpleIdentifier(String id) {
    this.addProperty(DC.identifier, id);
  }

  public void addComplexIdentifier(String id, String type, String issuer, Document doc) {
    if (id == null) return;

    Resource identifier = model.createResource(this.uri + "/id/" + id)
            .addProperty(RDF.type, CIDOC.E42_Identifier)
            .addProperty(RDFS.label, id)
            .addProperty(CIDOC.P2_has_type, type);

    Resource assignment = model.createResource(this.uri + "/id_assignment/" + id)
            .addProperty(RDF.type, CIDOC.E15_Identifier_Assignment)
            .addProperty(CIDOC.P37_assigned, identifier)
            .addProperty(CIDOC.P14_carried_out_by, issuer);

    doc.addProperty(CIDOC.P70_documents, assignment);
    this.addProperty(CIDOC.P1_is_identified_by, identifier);
  }

}
