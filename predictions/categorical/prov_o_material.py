from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob
import uuid
import time
#           prov:used ?dig .
#           ?dig a crmdig:D1_Digital_Object .
#           ?dig ecrm:P129_is_about ?object .
counter_pred = 0
counter_act = 0
for file_name in glob.glob('material_group.tsv'):
    x = pd.read_csv(file_name,sep="\t")
    for index, row in x.iterrows():
        score = row['score']
        predicted = row['predicted']
        obj = row['obj']
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
           prov:atTime "2021-05-21"^^xsd:dateTime ;
           prov:used ?dig .
           ?statement prov:wasGeneratedBy ?activity .

           ?actor a prov:SoftwareAgent ;
           ecrm:P70_documents """
        j = '''"Predictions made using a machine learning algorithm called Gradient Tree Boosting. The software predicts values for four properties, namely 'production timespan', 'production place', 'technique' and 'material'. The predictions are based on the values that are already present for these properties together with the museum name. It is therefore used to fill 'gaps', i.e. missing values for some properties, in the records of the Knowledge Graph and trained with data already present in it. This version of the models is trained on a snapshot of the Knowledge Graph from May 2021. The single task multi-class classification (mutually exclusive classes) models make use of a softmax function, the maximum value output of which corresponds to the provided prediction score."''' + " ."
        k = """
            ?activity prov:wasAssociatedWith ?actor .
            }
            WHERE {
            VALUES ?object { """
        l = "<"+str(obj)+">"
        m = """}
           ?production ecrm:P108_has_produced ?object .
           ?object rdfs:comment ?text .
           ?dig a crmdig:D1_Digital_Object .
           ?dig ecrm:P129_is_about ?object .


           BIND(URI(REPLACE(CONCAT("http://data.silknow.org", "/statement/"""
        n = str(uuid.uuid5(uuid.NAMESPACE_DNS, str(str(obj)+str(predicted)+str(score)+str(file_name))))
        o = """"), "object", "prediction", "i")) AS ?statement)
            BIND(URI(REPLACE(CONCAT("http://data.silknow.org", "/actor/XGBoost-classifier/"""
        r = """"), "object", "prediction", "i")) AS ?actor)
            BIND(URI(CONCAT("http://data.silknow.org", "/activity/"""
        s = str(uuid.uuid5(uuid.NAMESPACE_DNS, str(str(obj)+str(predicted)+str(score)+str(file_name))))
        t = """")) AS ?activity)
            } """


        q = a + b + c + d + e + f + g + j + k + l + m + n + o + r + s + t
        print(q.strip())

        
        sparql.setQuery(q.strip())
        sparql.setReturnFormat(RDFXML)

        results = sparql.query().convert()
        results.serialize(destination="./material/"+"image_material"+str(index)+".ttl", format="turtle")
        
        ##try:
        ##    results = sparql.query().convert()
        ##    results.serialize(destination="./material/"+"xgboost_material"+str(index)+".ttl", format="turtle")
        ##except:
        ##    time.sleep(10)
        ##    continue
