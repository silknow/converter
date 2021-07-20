from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob
import uuid
import time

counter_pred = 0
counter_act = 0
for file_name in glob.glob('place_country_code.tsv'):
    x = pd.read_csv(file_name,sep="\t")
    for index, row in x.iterrows():
        score = row['score']
        predicted = row['predicted']
        obj = row['obj']
        counter_pred = counter_pred + 1 
        counter_act = counter_act + 1

        sparql = SPARQLWrapper("http://data.silknow.org/sparql")
        if predicted == "BE":
           predicted = "https://sws.geonames.org/2802361/"
        if predicted == "CN":
           predicted = "https://sws.geonames.org/1814991/"
        if predicted == "DE":
           predicted = "https://sws.geonames.org/2921044/"
        if predicted == "EG":
           predicted = "https://sws.geonames.org/357994/"
        if predicted == "ES":
           predicted = "https://sws.geonames.org/2510769/"
        if predicted == "FR":
           predicted = "https://sws.geonames.org/3017382/"
        if predicted == "GB":
           predicted = "https://sws.geonames.org/2635167/"
        if predicted == "IN":
           predicted = "https://sws.geonames.org/1269750/"
        if predicted == "IR":
           predicted = "https://sws.geonames.org/130758/"
        if predicted == "IT":
           predicted = "https://sws.geonames.org/3175395/"
        if predicted == "JP":
           predicted = "https://sws.geonames.org/1861060/"
        if predicted == "NL":
           predicted = "https://sws.geonames.org/2750405/"
        if predicted == "RU":
           predicted = "https://sws.geonames.org/2017370/"
        if predicted == "TR":
           predicted = "https://sws.geonames.org/298795/"
        if predicted == "JM":
           predicted = "https://sws.geonames.org/3489940/"
        if predicted == "GR":
           predicted = "https://sws.geonames.org/390903/"
        if predicted == "US":
           predicted = "https://sws.geonames.org/6252001/"
        if predicted == "PK":
           predicted = "https://sws.geonames.org/1168579/"



           
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
           
           ?statement rdf:predicate ecrm:P8_took_place_on_or_within .
           ?production ecrm:P8_took_place_on_or_within """
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
            results.serialize(destination="./place/"+"text_place"+str(index)+".ttl", format="turtle")
        except:
            time.sleep(10)
            continue
