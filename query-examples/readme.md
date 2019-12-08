This folder contains query examples illustrating SILKNOW data available in the [SILKNOW SPARQL Endpoint](http://data.silknow.org/data).

Some queries have only a _partial_ answer or no answer at all since the modeling and the publication of data is still a work in progress.

## Table of Contents
* [A. Location](#location)
* [B. Time](#time)
* [C. Materials](#materials)
* [D. Artists](#artists)
* [F. Type of items and location](#typeofitemsandlocation)

<a name="location"/>

## A. LOCATION


1. **[en]** Which items were produced in Spain?   
[query](./1.rq) - [results]()

1. **[en]** Give me all the items that are preserved in the Musée des Tissus de Lyon  
[query](./2.rq) - [results]()

1. **[en]** In which museums and collections around the world are Spanish textiles?
[query](./3.rq) - [results]()

<!-- END Location -->

<a name="time"/>

## B. Time

1. **[en]** Which items were produced during the 16th century?
[query](./4.rq) - [results]

1. **[en]** What kinds of fabrics / weaving techniques / designs were most frequent in 18th-century France? Please give me a list of the top 5 (or 10, 15…) occurrences in a particular field.
[query](./5.rq) - [results]()

1. **[en]** Which items have been produced in 1815?
[query](./6.rq) - [results]

1. **[en]** What are the most common decorative motifs in the Hispanic Middle Ages?
[query](./7.rq) - [results]


<!-- END Time -->

<a name="materials"/>

## C. Materials

1. **[en]** Which items were produced with silk and silver?  
[query](./8.rq) - [results]()

1. **[en]** Give me the objects that involve at most silk, silver and wool
[query](./9.rq) - [results]()

1. **[en]** Give me the objects that involve silk, silver and wool, except those that involve gold.
[query](./10.rq) - [results]()


<!-- END Material -->

<a name="artists"/>

## D. Artists

1. **[en]** Give me all the information you have on Philippe de la Salle!  
[query](./11.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fartist+SAMPLE%28%3FartistName%29+as+%3Fname+COUNT%28DISTINCT+%3Frec%29+as+%3Frecording_num%0D%0AWHERE+%7B%0D%0A+%3Frec++a+efrbroo%3AF29_Recording_Event+%3B%0D%0A+++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Forganization%2FRadio_France%3E+%3B%0D%0A+++++++efrbroo%3AR20_recorded+%3Fperformance+.%0D%0A%0D%0A+%3Fperformance+ecrm%3AP9_consists_of*+%2F+ecrm%3AP14_carried_out_by+%3Fartist+.%0D%0A%0D%0A+%3Fartist+foaf%3Aname+%3FartistName%0D%0A%7D+GROUP+BY+%3Fartist%0D%0AHAVING+%28COUNT%28DISTINCT+%3Frec%29+%3E+10%29%0D%0ALIMIT+100&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

<!-- END Artists -->

<a name="typeofitemsandlocation"/>

## E. Types of items and location

1. **[en]** What textiles belonged to the collector Mariano Fortuny? 
[query](./12.rq) - [results]() 


<!-- END Types of items and location -->
