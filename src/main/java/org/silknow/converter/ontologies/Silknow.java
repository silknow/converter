/* CVS $Id: $ */
package org.silknow.converter.ontologies; 
import org.apache.jena.rdf.model.*;
import org.apache.jena.ontology.*;
 
/**
 * Vocabulary definitions from https://raw.githubusercontent.com/silknow/knowledge-base/master/vocabularies/ontology/silknow-v1.0.ttl 
 * @author Auto-generated by schemagen on 21 Apr 2021 21:38 
 */
public class Silknow {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://data.silknow.org/ontology/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>The ontology's owl:versionInfo as a string</p> */
    public static final String VERSION_INFO = "Version 1.0";
    
    public static final ObjectProperty L1 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L1" );
    
    public static final ObjectProperty L10 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L10" );
    
    public static final ObjectProperty L11 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L11" );
    
    public static final ObjectProperty L12 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L12" );
    
    public static final ObjectProperty L13 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L13" );
    
    public static final ObjectProperty L14 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L14" );
    
    public static final ObjectProperty L15 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L15" );
    
    public static final ObjectProperty L16 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L16" );
    
    public static final ObjectProperty L17 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L17" );
    
    public static final ObjectProperty L18 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L18" );
    
    public static final ObjectProperty L2 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L2" );
    
    public static final ObjectProperty L3 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L3" );
    
    public static final ObjectProperty L4 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L4" );
    
    public static final ObjectProperty L5 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L5" );
    
    public static final ObjectProperty L6 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L6" );
    
    public static final ObjectProperty L7 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L7" );
    
    public static final ObjectProperty L8 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L8" );
    
    public static final ObjectProperty L9 = m_model.createObjectProperty( "http://data.silknow.org/ontology/L9" );
    
    public static final OntClass T1 = m_model.createClass( "http://data.silknow.org/ontology/T1" );
    
    public static final OntClass T10 = m_model.createClass( "http://data.silknow.org/ontology/T10" );
    
    public static final OntClass T11 = m_model.createClass( "http://data.silknow.org/ontology/T11" );
    
    public static final OntClass T12 = m_model.createClass( "http://data.silknow.org/ontology/T12" );
    
    public static final OntClass T13 = m_model.createClass( "http://data.silknow.org/ontology/T13" );
    
    public static final OntClass T14 = m_model.createClass( "http://data.silknow.org/ontology/T14" );
    
    public static final OntClass T15 = m_model.createClass( "http://data.silknow.org/ontology/T15" );
    
    public static final OntClass T16 = m_model.createClass( "http://data.silknow.org/ontology/T16" );
    
    public static final OntClass T17 = m_model.createClass( "http://data.silknow.org/ontology/T17" );
    
    public static final OntClass T18 = m_model.createClass( "http://data.silknow.org/ontology/T18" );
    
    public static final OntClass T19 = m_model.createClass( "http://data.silknow.org/ontology/T19" );
    
    public static final OntClass T2 = m_model.createClass( "http://data.silknow.org/ontology/T2" );
    
    public static final OntClass T20 = m_model.createClass( "http://data.silknow.org/ontology/T20" );
    
    public static final OntClass T21 = m_model.createClass( "http://data.silknow.org/ontology/T21" );
    
    public static final OntClass T22 = m_model.createClass( "http://data.silknow.org/ontology/T22" );
    
    public static final OntClass T23 = m_model.createClass( "http://data.silknow.org/ontology/T23" );
    
    public static final OntClass T24 = m_model.createClass( "http://data.silknow.org/ontology/T24" );
    
    public static final OntClass T25 = m_model.createClass( "http://data.silknow.org/ontology/T25" );
    
    public static final OntClass T26 = m_model.createClass( "http://data.silknow.org/ontology/T26" );
    
    public static final OntClass T27 = m_model.createClass( "http://data.silknow.org/ontology/T27" );
    
    public static final OntClass T28 = m_model.createClass( "http://data.silknow.org/ontology/T28" );
    
    public static final OntClass T29 = m_model.createClass( "http://data.silknow.org/ontology/T29" );
    
    public static final OntClass T3 = m_model.createClass( "http://data.silknow.org/ontology/T3" );
    
    public static final OntClass T30 = m_model.createClass( "http://data.silknow.org/ontology/T30" );
    
    public static final OntClass T31 = m_model.createClass( "http://data.silknow.org/ontology/T31" );
    
    public static final OntClass T32 = m_model.createClass( "http://data.silknow.org/ontology/T32" );
    
    public static final OntClass T33 = m_model.createClass( "http://data.silknow.org/ontology/T33" );
    
    public static final OntClass T34 = m_model.createClass( "http://data.silknow.org/ontology/T34" );
    
    public static final OntClass T35 = m_model.createClass( "http://data.silknow.org/ontology/T35" );
    
    public static final OntClass T4 = m_model.createClass( "http://data.silknow.org/ontology/T4" );
    
    public static final OntClass T5 = m_model.createClass( "http://data.silknow.org/ontology/T5" );
    
    public static final OntClass T6 = m_model.createClass( "http://data.silknow.org/ontology/T6" );
    
    public static final OntClass T7 = m_model.createClass( "http://data.silknow.org/ontology/T7" );
    
    public static final OntClass T8 = m_model.createClass( "http://data.silknow.org/ontology/T8" );
    
    public static final OntClass T9 = m_model.createClass( "http://data.silknow.org/ontology/T9" );
    
}
