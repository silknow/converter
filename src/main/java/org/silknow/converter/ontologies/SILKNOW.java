package org.silknow.converter.ontologies;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;


public class SILKNOW {

  private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);


  public static final String NS = "http://data.silknow.org/ontology/";


  public static String getURI() {
    return NS;
  }


  public static final Resource NAMESPACE = m_model.createResource(NS);


  public static final ObjectProperty L1_Assigned_Object_type = m_model.createObjectProperty("http://data.silknow.org/L1_assigned_object_type");

  public static final ObjectProperty L4_Assigned_Domain_type = m_model.createObjectProperty("http://data.silknow.org/L4_assigned_domain_type");

 

  public static final OntClass T19_Object_Domain_Assignment = m_model.createClass("http://data.silknow.org/T19_Object_Domain_Assignment");

  public static final OntClass  T24_Pattern_Unit = m_model.createClass("http://data.silknow.org/T24_Pattern_Unit");

  public static final OntClass T35_Object_Type_Assignment = m_model.createClass("http://data.silknow.org/T35_Object_Type_Assignment");

}
