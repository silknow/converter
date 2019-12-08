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
[query](./1.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=%0D%0ASELECT+distinct++%3Fobj+%3Fname%0D%0AWHERE+%7B%0D%0A++++++%3Fs+a+ecrm%3AE12_Production+.%0D%0A++++++%3Fs+ecrm%3AP108_has_produced+%3Fobj.%0D%0A+++optional+%7B+++%3Fobj+ecrm%3AP3_has_note+%3Fname+.+%7D%0D%0A%0D%0A+%7B+%3Fs+ecrm%3AP8_took_place_on_or_within+%3Fl%0D%0A+++++%7B+SELECT+%3Fl+SAMPLE%28%3Floc%29+as+%3Fplace%0D%0A++++++++++WHERE+%7B+%3Fl+geonames%3AcountryCode+%3Floc%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fl%29%29%0D%0A+++++++++++%3Fl+geonames%3AcountryCode+%22ES%22++%7D%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Which items have been produced in Italy and are now preserved in France? (Note: "Moves" not well documented)   
[query](./2.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Ffrom+%3Fto+%3Fobj%0D%0AWHERE+%7B%0D%0A++++++%3Fs+a+ecrm%3AE9_Move+.%0D%0A++++++Optional+%7B%3Fs+ecrm%3AP25_moved+%3Fobj+.%7D%0D%0A+%0D%0A%0D%0A+%7B+%3Fs+ecrm%3AP27_moved_from+%3Fl%0D%0A+++++%7B+SELECT+%3Fl+SAMPLE%28%3Floc%29+as+%3Ffrom%0D%0A++++++++++WHERE+%7B+%3Fl+geonames%3AcountryCode+%3Floc%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fl%29%29%0D%0A+++++++++++%3Fl+geonames%3AcountryCode+%22IT%22%0D%0A+++++++++++%7D%0D%0A%0D%0A%7B+%3Ff+ecrm%3AP26_moved_to+%3Fx%0D%0A+++++%7B+SELECT+%3Fx+SAMPLE%28%3Floc2%29+as+%3Fto%0D%0A++++++++++WHERE+%7B+%3Fx+geonames%3AcountryCode+%3Floc2%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fx%29%29%0D%0A+++++++++++%3Fx+geonames%3AcountryCode+%22FR%22%0D%0A+++++++++++%7D%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me all the items that are preserved in the Musée des Tissus de Lyon  
[query](./3.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Fobject%0D%0AWHERE+%7B%0D%0AGRAPH+%3Chttp%3A%2F%2Fdata.silknow.org%2Fmtmad%3E%7B%0D%0A+%0D%0A+++++++++%3Fobject+a+ecrm%3AE22_Man-Made_Object+.+%0D%0A%7D%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** In which museums and collections around the world are Spanish textiles?
[query](./4.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Fobj+%3Fcollection%0D%0AWHERE+%7B+GRAPH+%3Fcollection+%7B%0D%0A++++++%3Fs+a+ecrm%3AE12_Production+.%0D%0A++++++%3Fs+ecrm%3AP108_has_produced+%3Fobj+.%0D%0A%0D%0A+%7B+%3Fs+ecrm%3AP8_took_place_on_or_within+%3Fl%0D%0A+++++%7B+SELECT+%3Fl+SAMPLE%28%3Floc%29+as+%3Fplace%0D%0A++++++++++WHERE+%7B+%3Fl+geonames%3AcountryCode+%3Floc%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fl%29%29%0D%0A+++++++++++%3Fl+geonames%3AcountryCode+%22ES%22+%7D%0D%0A%7D%7D&format=text%2Fhtml&timeout=0&debug=on)

<!-- END Location -->

<a name="time"/>

## B. Time

1. **[en]** Which items were produced during the 16th century?
[query](./5.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Fobj+%3Ftime%0D%0AWHERE+%7B%0D%0A%0D%0A+++++%3Fdig+a+crmdig%3AD1_Digital_Object+.%0D%0A+++++%3Fdig++ecrm%3AP129_is_about+%3Fprod+.%0D%0A+++++%3Fprod+ecrm%3AP108_has_produced+%3Fobj+.%0D%0A+%0D%0A%0D%0A%0D%0A%3Fprod+ecrm%3AP4_has_time-span+%3Ft+.%0D%0A%3Ft+ecrm%3AP78_is_identified_by+%3Ftime.%0D%0A%0D%0A%0D%0AFILTER+contains%28str%28%3Ftime%29%2C+%2216%22%29%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** What kinds of fabrics / weaving techniques / designs were most frequent in 18th-century France? Please give me a list of the top 5 (or 10, 15…) occurrences in a particular field.
[query](./6.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+count%28distinct+%3Fobj%29+as+%3Fcount++%3Fmaterial%0D%0AWHERE+%7B%0D%0A%0D%0A+++++%3Fdig+a+crmdig%3AD1_Digital_Object+.%0D%0A+++++%3Fdig++ecrm%3AP129_is_about+%3Fprd+.%0D%0A+++++%3Fprd+ecrm%3AP108_has_produced+%3Fobj+.%0D%0A+%0D%0A%7B+%3Fprd+ecrm%3AP126_employed+%3Fx%0D%0A+++++++%7B+SELECT+%3Fx+SAMPLE%28%3Flab%29+as+%3Fmaterial%0D%0A+++++++++WHERE+%7B+%3Fx+skos%3AprefLabel+%3Flab+.+%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fx%29%29+%7D%0D%0A+++++++UNION%0D%0A+++++++%7B+%3Fprd+ecrm%3AP126_employed+%3Fmaterial+.%0D%0A+++++++FILTER+%28isIRI%28%3Fmaterial%29+%3D+false%29%0D%0A++++++%7D%0D%0A%0D%0A%7B+%3Fprd+ecrm%3AP8_took_place_on_or_within+%3Fl%0D%0A+++++%7B+SELECT+%3Fl+SAMPLE%28%3Floc%29+as+%3Fplace%0D%0A++++++++++WHERE+%7B+%3Fl+geonames%3AcountryCode+%3Floc%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fl%29%29%0D%0A+++++++++++%3Fl+geonames%3AcountryCode+%22FR%22+%7D%0D%0A%0D%0A%3Fprd+ecrm%3AP4_has_time-span+%3Ft+.%0D%0A%3Ft+ecrm%3AP78_is_identified_by+%3Ftime.%0D%0A%0D%0A%0D%0AFILTER+contains%28str%28%3Ftime%29%2C+%2218%22%29%0D%0A%7D%0D%0AGROUP+BY+%3Fmaterial%0D%0AORDER+BY+DESC+%28%3Fcount%29&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Which items have been produced in 1815?
[query](./7.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Fobj+%3Ftime%0D%0AWHERE+%7B%0D%0A%0D%0A+++++%3Fdig+a+crmdig%3AD1_Digital_Object+.%0D%0A+++++%3Fdig++ecrm%3AP129_is_about+%3Fprod+.%0D%0A+++++%3Fprod+ecrm%3AP108_has_produced+%3Fobj+.%0D%0A+%0D%0A%0D%0A%0D%0A%3Fprod+ecrm%3AP4_has_time-span+%3Ft+.%0D%0A%3Ft+ecrm%3AP78_is_identified_by+%3Ftime.%0D%0A%0D%0A%0D%0AFILTER+%28str%28%3Ftime%29%3D%221815%22%29%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** What are the most common decorative motifs in the Hispanic Middle Ages?
[query](./8.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Fdepiction+count%28distinct+%3Fobj%29+as+%3Fcount+%3Ftime%0D%0AWHERE+%7B%0D%0A%0D%0A+++++%3Fdig+a+crmdig%3AD1_Digital_Object+.%0D%0A+++++%3Fdig++ecrm%3AP129_is_about+%3Fprod+.%0D%0A+++++%3Fprod+ecrm%3AP108_has_produced+%3Fobj+.%0D%0A+%0D%0A+++++%3Fobj+ecrm%3AP62_depicts+%3Fdepiction+.%0D%0A+++++FILTER%28lang%28%3Fdepiction%29+%3D+%22en%22%29%0D%0A%0D%0A%7B+%3Fprod+ecrm%3AP8_took_place_on_or_within+%3Fl%0D%0A+++++%7B+SELECT+%3Fl+SAMPLE%28%3Floc%29+as+%3Fplace%0D%0A++++++++++WHERE+%7B+%3Fl+geonames%3AcountryCode+%3Floc%7D%0D%0A+++++++%7D+.+FILTER%28isIRI%28%3Fl%29%29%0D%0A+++++++++++%3Fl+geonames%3AcountryCode+%22ES%22+%7D%0D%0A%0D%0A%3Fprod+ecrm%3AP4_has_time-span+%3Ft+.%0D%0A%3Ft+ecrm%3AP78_is_identified_by+%3Ftime.%0D%0A%0D%0A%0D%0AFILTER+contains%28str%28%3Ftime%29%2C+%22med%22%29%0D%0A%7D%0D%0AGROUP+BY+%3Fdepiction+%3Ftime%0D%0AORDER+BY+DESC+%28%3Fcount%29&format=text%2Fhtml&timeout=0&debug=on)


<!-- END Time -->

<a name="materials"/>

## C. Materials

1. **[en]** Which items were produced with silk and silver?  
[query](./9.rq) - [results]()

1. **[en]** Give me the objects that involve at most silk, silver and wool
[query](./10.rq) - [results]()

1. **[en]** Give me the objects that involve silk, silver and wool, except those that involve gold.
[query](./11.rq) - [results]()


<!-- END Material -->

<a name="artists"/>

## D. Artists

1. **[en]** Give me all the information you have on Philippe de la Salle!  
[query](./12.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fartist+SAMPLE%28%3FartistName%29+as+%3Fname+COUNT%28DISTINCT+%3Frec%29+as+%3Frecording_num%0D%0AWHERE+%7B%0D%0A+%3Frec++a+efrbroo%3AF29_Recording_Event+%3B%0D%0A+++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Forganization%2FRadio_France%3E+%3B%0D%0A+++++++efrbroo%3AR20_recorded+%3Fperformance+.%0D%0A%0D%0A+%3Fperformance+ecrm%3AP9_consists_of*+%2F+ecrm%3AP14_carried_out_by+%3Fartist+.%0D%0A%0D%0A+%3Fartist+foaf%3Aname+%3FartistName%0D%0A%7D+GROUP+BY+%3Fartist%0D%0AHAVING+%28COUNT%28DISTINCT+%3Frec%29+%3E+10%29%0D%0ALIMIT+100&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

<!-- END Artists -->

<a name="typeofitemsandlocation"/>

## E. Types of items and location

1. **[en]** What textiles belonged to the collector Mariano Fortuny? 
[query](./13.rq) - [results](http://data.silknow.org/sparql?default-graph-uri=&query=SELECT+distinct+%3Fobj+%3Factor+%3Ftex%0D%0AWHERE+%7B%0D%0A%0D%0A+++++%3Fdig+a+crmdig%3AD1_Digital_Object.%0D%0A+++++%3Fdig+ecrm%3AP129_is_about+%3Fobj+.%0D%0A%0D%0A+++++%3Fact+a+ecrm%3AE39_Actor+.%0D%0A+++++%3Fact+ecrm%3AP1_is_identified_by+%3Factor+.%0D%0A%0D%0A+++++%3Fdig+ecrm%3AP129_is_about+%3Fas+.%0D%0A+++++%3Fas+ecrm%3AP42_assigned+%3Ftex+.%0D%0A%0D%0AFILTER+%28contains%28str%28%3Factor%29%2C+%22Mariano%22%29+%7C%7C+contains%28str%28%3Factor%29%2C+%22Fortuny%22%29+%26%26+contains%28str%28%3Ftex%29%2C+%22text%22%29%29+%0D%0A+++++%0D%0A%7D%0D%0A%0D%0A%0D%0A%0D%0A%0D%0A&format=text%2Fhtml&timeout=0&debug=on) 


<!-- END Types of items and location -->
