from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob


for file_name in glob.glob('time.csv'):
    x = pd.read_csv(file_name)
    for index, row in x.iterrows():
        score = row['score']
        predicted = row['predicted']
        obj = row['obj']
        
        
        sparql = SPARQLWrapper("http://data.silknow.org/sparql")

        a = """
        
            prefix silk:  <http://data.silknow.org/ontology/>
            prefix crmsci: <http://www.ics.forth.gr/isl/CRMsci/>
            prefix crmdig: <http://www.ics.forth.gr/isl/CRMext/CRMdig.rdfs/>
            prefix prov: <http://www.w3.org/ns/prov#> 
            prefix xsd:  <http://www.w3.org/2001/XMLSchema#> 
            prefix :     <http://example.com/>


            CONSTRUCT {
           ?statement a rdf:Statement .
           ?statement rdf:subject ?production .
           ?statement rdf:object """
           
        b = "<"+str(predicted)+"> ."

        c =  """
           
           ?statement rdf:predicate rdf:predicate ecrm:P7_took_place_at .
           ?statement ecrm:P43_has_dimension ?dimension .

           
           ?dimension a ecrm:E54_Dimension .
           ?dimension ecrm:P2_has_type "Confidence Score" .
           ?dimension ecrm:P90_has_value
           """
        f = '"'+str(score) +'"'+"^^xsd:float ."
        g = """
            :statement_generation a prov:Activity, :Statement_generation;
           prov:AtTime "2021-02-10"^^xsd:dateTime;
           prov:used ?text .
           ?statement prov:WasGeneratedBy :Statement_generation .
           
           :text_analysis_algorithms a prov:Agent, :Text_analysis_algorithms;
           prov:type prov:SoftwareAgent ;
           ecrm:P70_documents """
        j = '"document"' + " ."
        k = """
            :statement_generation prov:wasAssociatedWith :Text_analysis_algorithms .
            }
            WHERE {
            VALUES ?object {
            """
        l = "<"+str(obj)+">"
        m = """
            }
           ?production ecrm:P108_has_produced ?object .
           ?object rdfs:comment ?text .
           BIND(URI(CONCAT(STR(?production), "/statement")) AS ?statement)
           BIND(URI(CONCAT(STR(?production), "/statement/dimension")) AS ?dimension)
            } """


        q = a + b + c + f + g + j + k + l + m
        print(q)

        
        sparql.setQuery(q)
        sparql.setReturnFormat(RDFXML)
        results = sparql.query().convert()

        results.serialize(destination="./"+"place"+str(index)+".ttl", format="turtle")
