This folder contains query examples illustrating SILKNOW data available in the [SILKNOW SPARQL Endpoint](http://data.silknow.org/data).

Some queries have only a _partial_ answer or no answer at all since the modeling and the publication of data is still a work in progress.

## Table of Contents
* [A. Location](#location)
* [B. Time](#time)
* [C. Materials](#materials)
* [D. Artists](#artists)
* [F. Type of items and location](#Typeofitemsandlocation)

<a name="location"/>

## A. LOCATION

1.**[en]** WWhich items were produced in Spain?   
[query](./1.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=PREFIX+mus%3A+%3Chttp%3A%2F%2Fdata.doremus.org%2Fontology%23%3E%0D%0APREFIX+ecrm%3A+%3Chttp%3A%2F%2Ferlangen-crm.org%2Fcurrent%2F%3E%0D%0APREFIX+efrbroo%3A+%3Chttp%3A%2F%2Ferlangen-crm.org%2Fefrbroo%2F%3E%0D%0APREFIX+skos%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23%3E%0D%0A%0D%0A%23+%5Ben%5D+Which+works+have+been+composed+by+Mozart%3F%0D%0A%23+%5Bfr%5D+Quelles+oeuvres+ont+%C3%A9t%C3%A9+compos%C3%A9es+par+Mozart+%3F%0D%0A%0D%0ASELECT+DISTINCT+%3Fexpression+SAMPLE%28%3Ftitle%29+as+%3Ftitle%0D%0AWHERE+%7B%0D%0A++%3Fexpression+a+efrbroo%3AF22_Self-Contained_Expression+%3B%0D%0A++++++++++rdfs%3Alabel+%3Ftitle+.%0D%0A++%3FexpCreation+efrbroo%3AR17_created+%3Fexpression+%3B%0D%0A++++++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Fcomposer+.%0D%0A++%3Fcomposer+foaf%3Aname+%22Wolfgang+Amadeus+Mozart%22%0D%0A%7D+ORDER+BY+%3Ftitle%0D%0A&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me all the items that are preserved in the Musée des Tissus de Lyon  
[query](./2.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=PREFIX+mus%3A+%3Chttp%3A%2F%2Fdata.doremus.org%2Fontology%23%3E%0D%0APREFIX+ecrm%3A+%3Chttp%3A%2F%2Ferlangen-crm.org%2Fcurrent%2F%3E%0D%0APREFIX+efrbroo%3A+%3Chttp%3A%2F%2Ferlangen-crm.org%2Fefrbroo%2F%3E%0D%0APREFIX+skos%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23%3E%0D%0A%0D%0A%23+%5Ben%5D+Which+works+have+been+composed+in+1836+%3F%0D%0A%23+%5Bfr%5D+Quelles+oeuvres+ont+%C3%A9t%C3%A9+compos%C3%A9es+en+1836+%3F%0D%0A%0D%0A%0D%0ASELECT+DISTINCT+%3Fexpression%2C+SAMPLE%28%3Ftitle%29+as+%3Ftitle%2C+%3Fstart%2C+%3Fend%0D%0AWHERE+%7B%0D%0A++%3Fexpression+a+efrbroo%3AF22_Self-Contained_Expression+%3B%0D%0A++++++++++rdfs%3Alabel+%3Ftitle+.%0D%0A++%3FexpCreation+efrbroo%3AR17_created+%3Fexpression+%3B%0D%0A++++++++++ecrm%3AP4_has_time-span+%3Fts.%0D%0A++%3Fts+time%3AhasEnd+%2F+time%3AinXSDDate+%3Fend+%3B%0D%0A++++++++++time%3AhasBeginning+%2F+time%3AinXSDDate+%3Fstart+.%0D%0A++FILTER+%28+%3Fstart+%3C%3D+%221836%22%5E%5Exsd%3AgYear+AND+%3Fend+%3E%3D+%221836%22%5E%5Exsd%3AgYear+%29%0D%0A%7D+ORDER+BY+%3Fstart%0D%0A&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** In which museums and collections around the world are Spanish textiles?
[query](./3.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fexpression%2C+SAMPLE%28%3Ftitle%29+as+%3Ftitle%2C+%3Fcasting%0D%0AWHERE+%7B%0D%0A++%3Fexpression+a+efrbroo%3AF22_Self-Contained_Expression+%3B%0D%0A++++++++++rdfs%3Alabel+%3Ftitle+%3B%0D%0A++++++++++mus%3AU13_has_casting+%3Fcasting+.%0D%0A%0D%0A++++%3Fcasting+mus%3AU23_has_casting_detail+%3FallCastingDets+.%0D%0A%0D%0A++++%3Fcasting+mus%3AU23_has_casting_detail+%3FcastingDet1+.%0D%0A++++%3FcastingDet1+mus%3AU2_foresees_use_of_medium_of_performance+%3Fviolin+%3B%0D%0A++++++++++++mus%3AU30_foresees_quantity_of_mop+2+.%0D%0A%0D%0A%0D%0A++++%3Fcasting+mus%3AU23_has_casting_detail+%3FcastingDet2+.%0D%0A++++%3FcastingDet2+mus%3AU2_foresees_use_of_medium_of_performance+%3Fviola+%3B%0D%0A++++++++++++mus%3AU30_foresees_quantity_of_mop+1+.%0D%0A%0D%0A++++%3Fcasting+mus%3AU23_has_casting_detail+%3FcastingDet3+.%0D%0A++++%3FcastingDet3+mus%3AU2_foresees_use_of_medium_of_performance+%3Fcello+%3B%0D%0A++++++++++++mus%3AU30_foresees_quantity_of_mop+1+.%0D%0A%0D%0A++VALUES+%28%3Fviolin%29+%7B+%28+%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fmop%2Fsvl%3E+%29+%28%3Chttp%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F3573%3E%29+%7D%0D%0A++VALUES+%28%3Fviola%29+%7B+%28%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fmop%2Fsva%3E%29+%28%3Chttp%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F3561%3E%29+%7D%0D%0A++VALUES+%28%3Fcello%29+%7B+%28%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fmop%2Fsvc%3E%29+%28%3Chttp%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F3582%3E%29+%7D%0D%0A%0D%0A%7D%0D%0AGROUP+BY+%3Fexpression+%3Fcasting%0D%0AHAVING+%28COUNT%28%3FallCastingDets%29+%3D+3%29%0D%0A&format=text%2Fhtml&timeout=0&debug=on)

<!-- END Location -->

<a name="time"/>

## B. Time

1.**[en]** Which items were produced during the 16th century?
  [query](./4.rq) - [results]

1. **[en]** What kinds of fabrics / weaving techniques / designs were most frequent in 18th-century France? Please give me a list of the top 5 (or 10, 15…) occurrences in a particular field.

[query](./5.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fartist+%3Fname+%3FbirthDate%0D%0AWHERE+%7B%0D%0A++%3Fperformance+ecrm%3AP9_consists_of+%3Fpart+.%0D%0A%0D%0A++%3Fpart+ecrm%3AP14_carried_out_by+%3Fartist+%3B%0D%0A++++++mus%3AU1_used_medium_of_performance+%2F+skos%3AexactMatch*+%3Chttp%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F4232%3E+.%0D%0A%0D%0A++%3Fartist+rdfs%3Alabel+%3Fname+.%0D%0A++OPTIONAL+%7B+%3Fartist+schema%3AbirthDate+%3FbirthDate+%7D%0D%0A%7D+ORDER+BY+%3Fartist%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Which items have been produced in 1815?
[query](./6.rq) - [results]

1. **[en]** What are the most common decorative motifs in the Hispanic Middle Ages?
[query](./7.rq) - [results]


<!-- END Time -->

<a name="materials"/>

## C. Materials

1. **[en]** Which items were produced with silk and silver?  
[query](./8.rq) - [results]

1. **[en]** Give me the objects that involve at most silk, silver and wool
[query](./9.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fexpression%2C+SAMPLE%28%3Ftitle%29+as+%3Ftitle%2C+%3FcomposerName%2C+%3Ffunction%2C+%3Fmop%2C+%3Fperformance%0D%0AWHERE+%7B%0D%0A++%3Fexpression+a+efrbroo%3AF22_Self-Contained_Expression+%3B%0D%0A++++++++++rdfs%3Alabel+%3Ftitle+.%0D%0A++++++++++%0D%0A++%3FexpCreation+efrbroo%3AR17_created+%3Fexpression+%3B%0D%0A++++++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Fcomposer+.%0D%0A++++++++++%0D%0A++%3Fcomposer+foaf%3Aname+%3FcomposerName+.%0D%0A++%0D%0A++%3Fperformance+a+mus%3AM42_Performed_Expression_Creation+%3B%0D%0A++++efrbroo%3AR25_performed+%2F+ecrm%3AP165_incorporates+%3Fexpression+%3B%0D%0A++++ecrm%3AP9_consists_of+%3Factivity.%0D%0A++%0D%0A++%3Factivity+ecrm%3AP14_carried_out_by+%3Fcomposer+.%0D%0A++%0D%0A++OPTIONAL+%7B%0D%0A++++%3Factivity+mus%3AU35_foresees_function_of_type+%3Ffunction%0D%0A++%7D%0D%0A++OPTIONAL+%7B%0D%0A++++%3Factivity+mus%3AU1_used_medium_of_performance+%3Fmop%0D%0A++%7D%0D%0A%7D%0D%0A&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me the objects that involve silk, silver and wool, except those that involve gold.
[query](./10.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=PREFIX+mus%3A+%3Chttp%3A%2F%2Fdata.doremus.org%2Fontology%23%3E%0D%0APREFIX+ecrm%3A+%3Chttp%3A%2F%2Ferlangen-crm.org%2Fcurrent%2F%3E%0D%0APREFIX+efrbroo%3A+%3Chttp%3A%2F%2Ferlangen-crm.org%2Fefrbroo%2F%3E%0D%0APREFIX+skos%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23%3E%0D%0A%0D%0A%23+%5Ben%5D+Give+me+all+the+performances+in+which+a+composer+directs+one+of+his+works%0D%0A%23+%5Bfr%5D+Donne-moi+tous+les+performance+dans+lesquels+un+compositeur+dirige+une+de+ses+oeuvres%0D%0A%0D%0ASELECT+DISTINCT+%3Fexpression%2C+SAMPLE%28%3Ftitle%29+as+%3Ftitle%2C+%3Fname%2C+%3Fperformance%0D%0AWHERE+%7B%0D%0A++%3Fexpression+a+efrbroo%3AF22_Self-Contained_Expression+%3B%0D%0A++++++++++rdfs%3Alabel+%3Ftitle+.%0D%0A++%3FexpCreation+efrbroo%3AR17_created+%3Fexpression+%3B%0D%0A++++++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Fcomposer+.%0D%0A++%3Fcomposer+foaf%3Aname+%3Fname+.%0D%0A++%3Fperformance+efrbroo%3AR66_included_performed_version_of+%3Fexpression+%3B%0D%0A++++++++++ecrm%3AP9_consists_of+%3Factivity.%0D%0A++%3Factivity+ecrm%3AP14_carried_out_by+%3Fcomposer+%3B+%0D%0A+++++mus%3AU31_had_function%0D%0A++++++++++%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Ffunction%2Fchief_conductor%3E+.%0D%0A%7D%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)


<!-- END Material -->

<a name="artists"/>

## D. Artists

1.**[en]** Give me all the information you have on Philippe de la Salle!  
[query](./11.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fartist+SAMPLE%28%3FartistName%29+as+%3Fname+COUNT%28DISTINCT+%3Frec%29+as+%3Frecording_num%0D%0AWHERE+%7B%0D%0A+%3Frec++a+efrbroo%3AF29_Recording_Event+%3B%0D%0A+++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Forganization%2FRadio_France%3E+%3B%0D%0A+++++++efrbroo%3AR20_recorded+%3Fperformance+.%0D%0A%0D%0A+%3Fperformance+ecrm%3AP9_consists_of*+%2F+ecrm%3AP14_carried_out_by+%3Fartist+.%0D%0A%0D%0A+%3Fartist+foaf%3Aname+%3FartistName%0D%0A%7D+GROUP+BY+%3Fartist%0D%0AHAVING+%28COUNT%28DISTINCT+%3Frec%29+%3E+10%29%0D%0ALIMIT+100&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me the list of the choristers of the Collegium Vocale who participated in at least three radio recordings of the choir in 2012  
**[fr]** Donne-moi la liste des choristes du Collegium Vocale ayant participé à au moins trois enregistrements radio du choeur en 2012  
[query](./41.rq) - no data

1. **[en]** Give me the name of the vocal soloist most recorded by Radio France in 2014  
**[fr]** Donne-moi le nom du ou de la soliste vocale ayant le plus été enregistré(e) par Radio France en 2014  
[query](./42.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fartist+SAMPLE%28%3FartistName%29+as+%3Fname+COUNT%28DISTINCT+%3Frec%29+as+%3Frecording_num%0D%0AWHERE+%7B%0D%0A+%3Frec++a+efrbroo%3AF29_Recording_Event+%3B%0D%0A+++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Forganization%2FRadio_France%3E+%3B%0D%0A+++++++efrbroo%3AR20_recorded+%3Fperformance+.%0D%0A%0D%0A+%3Fperformance+ecrm%3AP9_consists_of*+%3Fpart%3B%0D%0A++++ecrm%3AP4_has_time-span+%2F+time%3AhasBeginning+%2F+time%3AinXSDDate+%3Ftime+.%0D%0A%0D%0A+%3Fpart+ecrm%3AP14_carried_out_by+%3Fartist+%3B%0D%0A++++mus%3AU1_used_medium_of_performance+%2F+skos%3Abroader*+%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fmop%2Fv%3E+.%0D%0A%0D%0A+%3Fartist+foaf%3Aname+%3FartistName%0D%0A%0D%0A+FILTER+%28+year%28%3Ftime%29+%3D+2014+%29%0D%0A%0D%0A%7D+GROUP+BY+%3Fartist%0D%0AORDER+BY+DESC+%28COUNT%28DISTINCT+%3Frec%29%29%0D%0ALIMIT+1&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me the list of all the concerts recorded by Radio France at the Cité de la Musique between 1995 and 2014  
**[fr]** Donne-moi la liste de tous les concerts enregistrés par Radio France à la Cité de la Musique entre 1995 et 2014  
 [query](./43.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fconcert+SAMPLE%28%3Ftitle%29+year%28%3Ftime%29%0D%0AWHERE+%7B%0D%0A+%3Frec++a+efrbroo%3AF29_Recording_Event+%3B%0D%0A+++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Forganization%2FRadio_France%3E+%3B%0D%0A+++++++ecrm%3AP7_took_place_at+%3Fplace+%3B%0D%0A+++++++ecrm%3AP4_has_time-span+%2F+time%3AhasBeginning+%2F+time%3AinXSDDate+%3Ftime+%3B%0D%0A+++++++efrbroo%3AR20_recorded+%3Fconcert+.%0D%0A%0D%0A+%3Chttp%3A%2F%2Fdata.doremus.org%2Fplace%2Fbd21be9c-3f2b-3aa3-a460-114d579eabe6%3E+owl%3AsameAs+%3Fplace.%0D%0A%0D%0A%0D%0A+%3Fconcert+a+efrbroo%3AF31_Performance%3B%0D%0A++rdfs%3Alabel+%3Ftitle.%0D%0A%0D%0A+FILTER+%28year%28%3Ftime%29+%3E%3D+1995+AND+year%28%3Ftime%29+%3C%3D+2015+%29%0D%0A%7D+LIMIT+500%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me the list of concerts recorded by Radio France at the auditorium of the Cité de la Musique in which were used one or several French harpsichords of the 17th century belonging to the Musée de la Musique  
**[fr]** Donne-moi la liste des concerts enregistrés par Radio France à l’auditorium de la Cité de la Musique dans lesquels étaient utilisés un ou plusieurs clavecins français du XVIIe siècle appartenant au Musée de la Musique  

1. **[en]** Give me the list of the recordings made in 2014 by Harmonia Mundi with French musical ensembles, using at least one Urtext score  
**[fr]** Donne-moi la liste des enregistrements réalisés en 2014 par Harmonia Mundi avec des ensembles musicaux français, utilisant au moins une partition Urtext  
[query](./44.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Frec+%3Fensemble+sample%28%3Fensemble_name%29+as+%3Fname%0D%0AWHERE+%7B%0D%0A+%3Frec++a+efrbroo%3AF29_Recording_Event+%3B%0D%0A+++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Fartist%2F91eaff1a-0683-3ce6-86a2-5741f1677777%3E+%3B%0D%0A+++++++efrbroo%3AR20_recorded+%3Fconcert+.%0D%0A%0D%0A+%3Fconcert+a+efrbroo%3AF31_Performance%3B%0D%0A+++ecrm%3AP9_consists_of*+%3Fpart+%3B%0D%0A+++ecrm%3AP9_consists_of+%2F+ecrm%3AP16_used_specific_object+%5B+%0D%0A++++++++++++++mus%3AU221_has_title_proper_of_series%09%22Urtext%22++%5D+.%0D%0A%0D%0A++%3Fpart+ecrm%3AP14_carried_out_by+%3Fensemble+%3B%0D%0A++++mus%3AU1_used_medium_of_performance+%2F+skos%3Abroader*+%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fmop%2Fo%3E+.%0D%0A%0D%0A++%3Fensemble+rdfs%3Alabel+%3Fensemble_name%3B%0D%0A++++++++++++ecrm%3AP74_has_current_or_former_residence+%2F+geonames%3AcountryCode+%27FR%27+.%0D%0A%7D+LIMIT+100%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on) (no data)

1. **[en]** Give me all the registration free of rights  
**[fr]** Donne-moi tous les enregistrement libre de droit  

1. **[en]** Give me the cutting of all the recordings of Don Giovanni by Mozart  
**[fr]** Donne moi le découpage de tous les enregistrements de Don Giovanni de Mozart  
[query](./45.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Ftrackset+%3Ftrack%0D%0AWHERE+%7B%0D%0A++%3Ftrackset+efrbroo%3AR5_has_component+%3Ftrack+.%0D%0A++%3Ftrack+a+mus%3AM24_Track+%3B%0D%0A++++++++mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of%09%3Fwork+.%0D%0A%0D%0A++%3Fwork+rdfs%3Alabel+%3Ftitle.%0D%0A%0D%0A++%3FexpCreation+efrbroo%3AR17_created+%3Fwork+%3B%0D%0A++++++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Fartist%2F4802a043-23bb-3b8d-a443-4a3bd22ccc63%3E+.%0D%0A%0D%0A++FILTER+%28contains%28str%28%3Ftitle%29%2C+%22Don+Giovanni%22%29%29%0D%0A%7D%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)


1. **[en]** Give me all the recordings of the _Catalogue Aria_ (isolated air or in a recording of the opera)  
**[fr]** Donne moi tous les enregistrements de l'_Air du catalogue_ (air isolé ou dans un enregistrement de l’opéra)  
[query](./46.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Frec%0D%0AWHERE+%7B%0D%0A++%3Frec+mus%3AU51_is_partial_or_full_recording_of+%3Fperf.%0D%0A%0D%0A++%3Fperf+rdfs%3Alabel+%3Flabel+%3B%0D%0A+++++++++mus%3AU54_is_performed_expression_of%09%3Fwork+.%0D%0A%0D%0A++%3Fwork+rdfs%3Alabel+%3Ftitle.%0D%0A%0D%0A++%3FexpCreation+efrbroo%3AR17_created+%3Fwork+%3B%0D%0A++++++++++ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by+%3Chttp%3A%2F%2Fdata.doremus.org%2Fartist%2F4802a043-23bb-3b8d-a443-4a3bd22ccc63%3E+.%0D%0A%0D%0A++FILTER+%28contains%28str%28%3Flabel%29%2C+%22Air+du+catalogue%22%29%29%0D%0A++FILTER+%28contains%28str%28%3Ftitle%29%2C+%22Don+Giovanni%22%29%29%0D%0A%7D&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

<!-- END Recordings -->

<a name="typeofitemsandlocation"/>

## E. Types of items and location

1. **[en]** Among concerts and CDs, which works are often played after < other work > ?  
**[fr]** Dans les concerts et les cd, quelles oeuvres sont souvent jouées après < telle autre oeuvre > ?  
[query](./47.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fwork2+SAMPLE%28%3Ftitle%29+as+%3Ftitle+%28COUNT%28distinct+%3Ft2%29%29+as+%3Fnum_times+SAMPLE%28%3Ft2%29+as+%3Fexample_track%0D%0AWHERE+%7B%0D%0A++%3Ftrackset+efrbroo%3AR5_has_component+%3Ft1%2C+%3Ft2+.%0D%0A%0D%0A++%3Ft1+mus%3AU10_has_order_number+%3Fon1+%3B%0D%0A++++++mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of+%3Fwork1.%0D%0A%0D%0A++%3Ft2+mus%3AU10_has_order_number+%3Fon2+%3B%0D%0A++++++mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of+%3Fwork2.%0D%0A%0D%0A++%3Fwork2+rdfs%3Alabel+%3Ftitle+.%0D%0A%0D%0A++VALUES+%3Fwork1+%7B+%3Chttp%3A%2F%2Fdata.doremus.org%2Fexpression%2Fd72301f0-0aba-3ba6-93e5-c4efbee9c6ea%3E+%7D%0D%0A++FILTER+%28%3Fwork1+%21%3D+%3Fwork2%29%0D%0A++FILTER+%28%3Fon2+%3D+%28%3Fon1+%2B+1%29%29%0D%0A%7D%0D%0AGROUP+BY+%3Fwork2%0D%0AORDER+BY+DESC+%28COUNT%28distinct+%3Ft2%29%29%0D%0ALIMIT+100%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on) (for Moonlight Sonata)

1. **[en]** Give me pairs of recorded tracks that are composed with the same key  
**[fr]** donne-moi des paires de titres enregistrés qui sont composés dans la même tonalité  
[query](./48.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fwork1+SAMPLE%28%3Ftitle1%29+as+%3Ftitle1+%3Fwork2+SAMPLE%28%3Ftitle2%29+as+%3Ftitle2+%3Fkey%0D%0AWHERE+%7B%0D%0A++%3Ftrackset+efrbroo%3AR5_has_component+%3Ft1%2C+%3Ft2+.%0D%0A%0D%0A++%3Ft1+mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of+%3Fwork1.%0D%0A++%3Ft2+mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of+%3Fwork2.%0D%0A%0D%0A++%3Fwork1+rdfs%3Alabel+%3Ftitle1+%3B+mus%3AU11_has_key+%3Fkey+.%0D%0A++%3Fwork2+rdfs%3Alabel+%3Ftitle2+%3B+mus%3AU11_has_key+%3Fkey+.%0D%0A%0D%0A++FILTER+%28%3Fwork1+%21%3D+%3Fwork2%29%0D%0A%7D+LIMIT+100%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

1. **[en]** Give me the list of the latest releases of DGG (Deutsche Grammophon Gesellschaft) in chamber music for strings  
**[fr]** Donne moi la liste des dernières parutions de DGG (Deutsche Grammophon Gesellschaft) en musique de chambre pour cordes  
[query](./49.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Fpublication+%3Fdate+%3Fgenre%0D%0AWHERE+%7B%0D%0A++%3Fpublication+ecrm%3AP9_consists_of+%2F+ecrm%3AP14_carried_out_by%09%3Chttp%3A%2F%2Fdata.doremus.org%2Fartist%2F448475b4-f31d-333b-8fc1-c18a011e8d6c%3E+%3B%0D%0A++ecrm%3AP4_has_time-span+%2F+time%3AhasBeginning+%2F+time%3AinXSDDate+%3Fdate+%3B%0D%0A++efrbroo%3AR24_created+%2F+mus%3AU58_has_full_published_recording+%5B%0D%0A++++++mus%3AU12_has_genre+%2F+skos%3AprefLabel+%3Fgenre+%3B%0D%0A++++++mus%3AU13_has_casting+%2F+mus%3AU23_has_casting_detail+%2F%0D%0A++++++++++mus%3AU2_foresees_use_of_medium_of_performance+%2F+skos%3Abroader*++%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fmop%2Fs%3E+%0D%0A++%5D+%0D%0A%0D%0A++FILTER+contains%28str%28%3Fgenre%29%2C+%22chambre%22%29%0D%0A%7D%0D%0AORDER+BY+DESC+%28%3Fdate%29%0D%0ALIMIT+100%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on) (no data for chamber music)

1. **[en]** Give me all the recordings of opera aria whose library has at least one score  
**[fr]** Donne moi tous les enregistrements d’airs d’opéra dont la bibliothèque dispose d’au moins une partition  
[query](./50.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Ft+%3Fwork%0D%0AWHERE+%7B%0D%0A++%3Ft+mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of+%3Fwork.%0D%0A++%3Fwork+mus%3AU12_has_genre+%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fgenre%2Fop%3E+.%0D%0A%0D%0A++%3Fscore+ecrm%3AP2_has_type+%22score%22%3B%0D%0A++++++ecrm%3AP128_carries+%3Fwork+.%0D%0A%7D+LIMIT+100%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on) (no data for opera)


1. **[en]** Give me all the recordings of opera aria whose library has no score  
**[fr]** Donne moi tous les enregistrements d’airs d’opéra dont la bibliothèque ne dispose d’aucune partition  
[query](./51.rq) - [results](http://data.doremus.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+%3Ft+%3Fwork%0D%0AWHERE+%7B%0D%0A++%3Ft+mus%3AU51_is_partial_or_full_recording_of+%2F+mus%3AU54_is_performed_expression_of+%3Fwork.%0D%0A++%3Fwork+mus%3AU12_has_genre+%3Chttp%3A%2F%2Fdata.doremus.org%2Fvocabulary%2Fiaml%2Fgenre%2Fop%3E+.%0D%0A%0D%0A++%3Fscore+ecrm%3AP2_has_type+%22score%22.%0D%0A++%0D%0A++FILTER%0D%0A++++NOT+EXISTS+%7B%3Fscore+ecrm%3AP128_carries+%3Fwork+%7D%0D%0A%7D+LIMIT+100%0D%0A&should-sponge=&format=text%2Fhtml&timeout=0&debug=on)

<!-- END Types of items and location -->
