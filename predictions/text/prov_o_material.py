from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob
import uuid
import time

counter_pred = 0
counter_act = 0
for file_name in glob.glob('material.tsv'):
    x = pd.read_csv(file_name,sep="\t")
    for index, row in x.iterrows():
        score = row['score']
        predicted = row['predicted']
        obj = row['id']
        counter_pred = counter_pred + 1 
        counter_act = counter_act + 1

        sparql = SPARQLWrapper("http://data.silknow.org/sparql")
        if predicted == "animal_fibre":
           predicted = "http://data.silknow.org/vocabulary/210"
        if predicted == "metal_thread":
           predicted = "http://data.silknow.org/vocabulary/497"
        if predicted == "vegetal_fibre":
           predicted = "http://data.silknow.org/vocabulary/214"
           
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
           
           ?statement rdf:predicate ecrm:P126_employed .
           ?production ecrm:P126_employed """
        d = "<"+str(predicted)+"> ."

        e = """

           ?statement silk:L18
           """
        f = '"'+str(score) +'"'+"^^xsd:float ."
        g = """
           ?activity a prov:Activity ;
           prov:atTime "2021-02-10"^^xsd:dateTime;
           prov:used ?text .
           ?statement prov:wasGeneratedBy ?activity .
           
           ?actor a prov:SoftwareAgent ;
           ecrm:P70_documents """
        j = '''"Predictions made using a CNN-based text classification software with cross-lingual word embeddings. Given an input text description of a silk object, the models available at https://zenodo.org/record/5070696 are able to predict values for four properties, namely 'production timespan', 'production place', 'technique' and 'material'. The models have bee trained based on a May 2021 snapshot of the Knowledge Graph. The single task multi-class classification (mutually exclusive classes) models have a softmax classification head, the maximum value output of which corresponds to the provided prediction score."''' + " ."
        k = """
            ?activity prov:wasAssociatedWith ?actor .
            }
            WHERE {
            VALUES ?object { """
        l = "<"+str(obj)+">"
        m = """}
           ?production ecrm:P108_has_produced ?object .
           ?object rdfs:comment ?text .

           BIND(URI(REPLACE(CONCAT("http://data.silknow.org", "/statement/"""
        n = str(uuid.uuid5(uuid.NAMESPACE_DNS, str(str(obj)+str(predicted)+str(score)+str(file_name))))
        o = """"), "object", "prediction", "i")) AS ?statement)
            BIND(URI(REPLACE(CONCAT("http://data.silknow.org", "/actor/jsi-text-analysis/1"""
        r = """"), "object", "prediction", "i")) AS ?actor)
            BIND(URI(CONCAT("http://data.silknow.org", "/activity/"""
        s = str(uuid.uuid5(uuid.NAMESPACE_DNS, str(str(obj)+str(predicted)+str(score)+str(file_name))))
        t = """")) AS ?activity)
            } """


        q = a + b + c + d + e + f + g + j + k + l + m + n + o + r + s + t
        print(q.strip())

        
        sparql.setQuery(q.strip())
        sparql.setReturnFormat(RDFXML)



        try:
            results = sparql.query().convert()
            results.serialize(destination="./material/"+"text_material"+str(index)+".ttl", format="turtle")
        except:
            time.sleep(10)
            continue
