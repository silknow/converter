from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob

counter_pred = 0
counter_act = 0
for file_name in glob.glob('sys_integration_pred_technique.csv'):
    x = pd.read_csv(file_name)
    for index, row in x.iterrows():
        score = row[' class_score'].strip()
        predicted = row[' predicted_class'].strip()
        obj = row['obj_uri'].strip()
        img = row[' image_name'].strip()
        counter_pred = counter_pred + 1 
        counter_act = counter_act + 1

        sparql = SPARQLWrapper("http://data.silknow.org/sparql")
        if predicted == "damask":
           predicted = "http://data.silknow.org/vocabulary/168"
        if predicted == "embroidery":
           predicted = "http://data.silknow.org/vocabulary/87"
        if predicted == "velvet":
           predicted = "http://data.silknow.org/vocabulary/379"
           
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
           
           ?statement rdf:predicate ecrm:P32_used_general_technique .
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

           BIND(URI(REPLACE(CONCAT(STR(?object), "/image/technique/"""
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

        results.serialize(destination="./technique/"+"image_technique"+str(index)+".ttl", format="turtle")
