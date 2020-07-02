package org.silknow.converter.entities;


import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.string2vocabulary.VocabularyManager;
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
  private int typeAssignmentCount;
  private Literal r;

  Entity() {
    // do nothing, enables customisation for child class
    this.model = ModelFactory.createDefaultModel();
    this.className = this.getClass().getSimpleName();

    this.activityCount = 0;
    this.observationCount = 0;
    this.typeAssignmentCount = 0;
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
    if (uri.equals(this.uri)) return;

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
    if (text == null) return;
    text = text.replaceAll("^- ", "").trim();
    this.addProperty(RDFS.comment, text, lang).addProperty(CIDOC.P3_has_note, text, lang);
  }

  public Resource addObservation(String text, String type, String lang) {
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
    if (actor == null || actor.equalsIgnoreCase("unknown")) return;
    this.addActivity(new Actor(actor), function);
  }

  public void addActivity(Actor actor, String function) {
    if (actor == null) return;

    Activity activity = new Activity(this.uri + "/activity/" + ++activityCount);
    activity.addActor(actor);

    if (function != null) activity.addProperty(CIDOC.P2_has_type, function);
    this.addProperty(CIDOC.P9_consists_of, activity);
  }

  protected void addSimpleIdentifier(String id) {
    this.addProperty(DC.identifier, id);
  }

  public Resource addComplexIdentifier(String id, String type) {
    return this.addComplexIdentifier(id, type, null, null);
  }

  public Resource addComplexIdentifier(String id, String type, LegalBody issuer) {
    return this.addComplexIdentifier(id, type, issuer, null);
  }

  public Resource addComplexIdentifier(String id, String type, LegalBody issuer, String replaceId) {
    if (id == null) return null;

    Resource identifier = model.createResource(this.uri + "/id/" + id.replaceAll(" ", "_"))
            .addProperty(RDF.type, CIDOC.E42_Identifier)
            .addProperty(RDFS.label, id)
            .addProperty(CIDOC.P2_has_type, type);

    Resource assignment = model.createResource(this.uri + "/id_assignment/" + id)
            .addProperty(RDF.type, CIDOC.E15_Identifier_Assignment)
            .addProperty(CIDOC.P37_assigned, identifier);
    if (issuer != null) {
      assignment.addProperty(CIDOC.P14_carried_out_by, issuer.asResource());
      this.model.add(issuer.getModel());
    }

    if (replaceId != null) {
      Resource rIdentifier = model.createResource(this.uri + "/id/" + replaceId.replaceAll(" ", "_"))
              .addProperty(RDF.type, CIDOC.E42_Identifier)
              .addProperty(RDFS.label, replaceId)
              .addProperty(CIDOC.P2_has_type, "old register number");
      assignment.addProperty(CIDOC.P38_deassigned, rIdentifier);
    }

    this.addProperty(CIDOC.P1_is_identified_by, identifier);
    return identifier;
  }

  public Resource addClassification(String classification, String type, String lang) {
    return addClassification(classification, type, lang,null);
  }


  public Resource addClassification(String classification, String type, String lang, LegalBody museum) {
    if (classification == null) return null;
    RDFNode r = VocabularyManager.searchInCategory(classification, null, "assigned", false);
    if (r == null) {
      r = model.createLiteral(classification);
    }


    Resource assignment = model.createResource(this.getUri() + "/type_assignment/" + ++typeAssignmentCount)
            .addProperty(RDF.type, CIDOC.E17_Type_Assignment)
            .addProperty(CIDOC.P41_classified, this.asResource())
            .addProperty(CIDOC.P42_assigned, r);

    if (type != null)
    {
      RDFNode t = VocabularyManager.searchInCategory(classification, null, "types", false);
      if (t != null) {
        assignment.addProperty(CIDOC.P2_has_type, t);
      }
      assignment.addProperty(CIDOC.P2_has_type, type, lang);
    }

    if (museum != null) {
      assignment.addProperty(CIDOC.P14_carried_out_by, museum.asResource());
      this.model.add(museum.getModel());
    }

    // this.addProperty(CIDOC.P2_has_type, classification);
    return assignment;
  }


  public String getId() {
    return this.id;
  }
}
