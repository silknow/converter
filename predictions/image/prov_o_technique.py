from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob
import uuid
import time

counter_pred = 0
counter_act = 0
for file_name in glob.glob('sys_integration_pred_technique.csv'):
    x = pd.read_csv(file_name)
    for index, row in x.iterrows():
        score = row[' class_score'].strip()
        predicted = row[' predicted_class'].strip()
        obj = row['obj_uri'].strip()
        img = row[' image_name'].strip()
        museum = row[' museum'].strip()
        image = "https://silknow.org/silknow/media/"+str(museum)+"/"+str(img)
        counter_pred = counter_pred + 1 
        counter_act = counter_act + 1

        sparql = SPARQLWrapper("http://data.silknow.org/sparql")
        if predicted == "damask":
           predicted = "http://data.silknow.org/vocabulary/168"
        if predicted == "embroidery":
           predicted = "http://data.silknow.org/vocabulary/87"
        if predicted == "velvet":
           predicted = "http://data.silknow.org/vocabulary/379"
        if predicted == "tabby":
           predicted = "http://data.silknow.org/vocabulary/236"
           



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
           ?production ecrm:P32_used_general_technique """
        d = "<"+str(predicted)+"> ."

        e = """

           ?statement silk:L18
           """
        f = '"'+str(float(score.strip('%'))/100) +'"'+"^^xsd:float ."
        g = """
           ?activity a prov:Activity ;
           prov:atTime "2021-02-10"^^xsd:dateTime;
           prov:used """
        x = "<"+str(image)+">" + " ."
        y = """
           ?statement prov:wasGeneratedBy ?activity .
           
           ?actor a prov:SoftwareAgent ;
           ecrm:P70_documents """
        j = '''"Predictions made using a CNN-based image classification software. Given an input image, the model, available at https://zenodo.org/record/4742418, is able to predict values for five properties, namely production 'timespan', 'production place', 'technique', 'material' and 'depiction'. It has been trained based on a February 2021 snapshot of the Knowledge Graph. The multi-task learning (MTL) variant is being used in a multi-class classification (mutually exclusive classes) fashion based on the softmax function for computing the class scores."''' + " ."
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
        n = str(uuid.uuid5(uuid.NAMESPACE_DNS, str(str(obj)+str(img)+str(predicted))))
        o = """"), "object", "prediction", "i")) AS ?statement)
            BIND(URI(REPLACE(CONCAT("http://data.silknow.org", "/actor/luh-image-analysis/1"""
        r = """"), "object", "prediction", "i")) AS ?actor)
            BIND(URI(CONCAT("http://data.silknow.org", "/activity/"""
        s = str(uuid.uuid5(uuid.NAMESPACE_DNS, str(str(obj)+str(img)+str(predicted))))
        t = """")) AS ?activity)
            } """


        q = a + b + c + d + e + f + g + x + y + j + k + l + m + n + o + r + s + t
        print(q.strip())

        
        sparql.setQuery(q.strip())
        sparql.setReturnFormat(RDFXML)
        try:
            results = sparql.query().convert()
            results.serialize(destination="./technique/"+"image_technique"+str(index)+".ttl", format="turtle")
        except:
            time.sleep(10)
            continue
