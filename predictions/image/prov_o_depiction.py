from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob
import uuid

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
        if predicted == "plant":
           predicted = "http://data.silknow.org/vocabulary/744"
        if predicted == "flower":
           predicted = "http://data.silknow.org/vocabulary/743"
        if predicted == "geometrical_shape":
           predicted = "http://data.silknow.org/vocabulary/745"
           
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
           ?production ecrm:P65_shows_visual_item """
        d = "<"+str(predicted)+"> ."

        e = """

           ?statement silk:L18
           """
        f = '"'+str(float(score.strip('%'))/100) +'"'+"^^xsd:float ."
        g = """
           ?activity a prov:Activity ;
           prov:AtTime "2021-02-10"^^xsd:dateTime;
           prov:used """
        x = "<"+str(img)+">" + " ."
        y = """
           ?statement prov:WasGeneratedBy ?activity .
           
           ?actor a prov:SoftwareAgent ;
           ecrm:P70_documents """
        j = '"Image classification is based on a convolutional neural network (CNN). First, a pre-trained backbone network is used as a generic feature extraction network; its output is processed by a series of fully connected network layers before a final classification layer delivers a probabilistic class score per variable and class. There are two basic variants of the CNN. The variant based on single-task learning (STL) consists of one branch only and predicts class scores for a single semantic variable only. Thus, in order to predict five variables, five different instances of the CNN have to be trained. On the other hand, the variant based on multi-task learning (MTL) has five classification branches following a common feature extractor, so that only one CNN has to be trained to predict all five variables simultaneously. Furthermore, for both network variants, there are two options for defining a classification task. The standard variant is a multi-class classification with mutually exclusive classes and based on the softmax function for computing the class scores; in this case, only one class label (the one achieving the largest class score) is selected to be the classification result. In addition, the software also can be configured to allow multiple binary classifications for a set of variables that can be configured by the user. In this case, for every possible class of a variable, a binary classification is carried out, predicting whether the image is consistent with that class or not. For instance, in this way the software can be configured to predict that multiple materials were used to produce a specific type of fabric. In all cases, the CNN is trained using training samples exported from the SILKNOW Knowledge Graph."' + " ."
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
        results = sparql.query().convert()

        results.serialize(destination="./depiction/"+"image_depiction"+str(index)+".ttl", format="turtle")
