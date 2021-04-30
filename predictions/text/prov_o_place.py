from SPARQLWrapper import SPARQLWrapper, RDFXML
from rdflib import Graph
import pandas as pd
import glob

counter_pred = 0
counter_act = 0
for file_name in glob.glob('place.csv'):
    x = pd.read_csv(file_name)
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
           prov:AtTime "2021-02-10"^^xsd:dateTime;
           prov:used ?text .
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

           BIND(URI(REPLACE(CONCAT(STR(?object), "/text/place/"""
        n = str(counter_pred)
        o = """"), "object", "prediction", "i")) AS ?statement)
            BIND(URI(REPLACE(CONCAT(STR(?object), "/actor/jsi-text-analysis/"""
        p = str(counter_act)
        r = """"), "object", "prediction", "i")) AS ?actor)
            BIND(URI(CONCAT(STR(?statement), "/generation")) AS ?activity)
            } """


        q = a + b + c + d + e + f + g + j + k + l + m + n + o + p + r
        print(q.strip())

        
        sparql.setQuery(q.strip())
        sparql.setReturnFormat(RDFXML)
        results = sparql.query().convert()

        results.serialize(destination="./place/"+"text_place"+str(index)+".ttl", format="turtle")
