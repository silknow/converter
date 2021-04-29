from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob

counter_pred = 0
counter_act = 0
for file_name in glob.glob('sys_integration_pred_depiction.csv'):
    x = pd.read_csv(file_name)
    for index, row in x.iterrows():
        score = row[' class_score'].strip()
        predicted = row[' predicted_class'].strip()
        obj = row['obj_uri'].strip()
        img = row[' image_name'].strip()
        counter_pred = counter_pred + 1 
        counter_act = counter_act + 1

        sparql = SPARQLWrapper("http://data.silknow.org/sparql")
        if predicted == "eighteenth_century_(dates_CE)":
           predicted = "http://vocab.getty.edu/aat/300404512"
        if predicted == "nineteenth_century_(dates_CE)":
           predicted = "http://vocab.getty.edu/aat/300404513"
        if predicted == "seventeenth_century_(dates_CE)":
           predicted = "http://vocab.getty.edu/aat/300404511"
        if predicted == "sixteenth_century_(dates_CE)":
           predicted = "http://vocab.getty.edu/aat/300404510"
        if predicted == "twentieth_century_(dates_CE)":
           predicted = "http://vocab.getty.edu/aat/300404514"
           
        a = """
        
            prefix silk:  <http://data.silknow.org/ontology/>
            prefix crmsci: <http://www.ics.forth.gr/isl/CRMsci/>
            prefix crmdig: <http://www.ics.forth.gr/isl/CRMext/CRMdig.rdfs/>
            prefix prov: <http://www.w3.org/ns/prov#> 
            prefix xsd:  <http://www.w3.org/2001/XMLSchema#> 



            CONSTRUCT {
           ?statement a rdf:Statement .
           ?statement rdf:subject ?production .
           ?statement rdf:object """
           
        b = "<"+str(predicted)+"> ."

        c =  """
           
           ?statement rdf:predicate ecrm:P65_shows_visual_item .
           ?statement silk:L18
           """
        f = '"'+str(score) +'" .'
        g = """
           ?activity a prov:Activity ;
           prov:AtTime "2021-02-10"^^xsd:dateTime;
           prov:used """
        x = "<"+str(img)+">" + " ."
        y = """
           ?statement prov:WasGeneratedBy ?activity .
           
           ?actor a prov:SoftwareAgen ;
           ecrm:P70_documents """
        j = '"document"' + " ."
        k = """
            ?activity prov:wasAssociatedWith ?actor .
            }
            WHERE {
            VALUES ?object { """
        l = "<"+str(obj)+">"
        m = """}
           ?production ecrm:P108_has_produced ?object .
           ?object rdfs:comment ?text .

           BIND(URI(REPLACE(CONCAT(STR(?object), "/image/depiction/"""
        n = str(counter_pred)
        o = """"), "object", "prediction", "i")) AS ?statement)
            BIND(URI(REPLACE(CONCAT(STR(?object), "/actor/luh-image-analysis/"""
        p = str(counter_act)
        r = """"), "object", "prediction", "i")) AS ?actor)
            BIND(URI(CONCAT(STR(?statement), "/generation")) AS ?activity)
            } """


        q = a + b + c + f + g + x + y + j + k + l + m + n + o + p + r
        print(q.strip())

        
        sparql.setQuery(q.strip())
        sparql.setReturnFormat(RDFXML)
        results = sparql.query().convert()

        results.serialize(destination="./depiction/"+"image_depiction"+str(index)+".ttl", format="turtle")
