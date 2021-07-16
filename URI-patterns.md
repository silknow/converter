URI patterns for SILKNOW data
==============================

This is the documentation of the URI design pattern used by the SILKNOW Knowledge Graph. See originally the [issue #42](https://github.com/silknow/converter/issues/42). The following SPARQL query provides the number of instances of each type ([results](https://data.silknow.org/sparql?default-graph-uri=&query=SELECT+DISTINCT+count%28%3Fs%29+as+%3Fnb+%3Ftype%0D%0AWHERE+%7B%0D%0A++%3Fs+a+%3Ftype+.%0D%0A++FILTER+%28contains%28str%28%3Ftype%29%2C+%22erlangen%22%29+%7C%7C+contains%28str%28%3Ftype%29%2C+%22forth%22%29%29%0D%0A%7D%0D%0AGROUP+BY+%3Ftype%0D%0AORDER+BY+DESC%28%3Fnb%29&should-sponge=&format=text%2Fhtml&timeout=0&debug=on&run=+Run+Query+)):

``` sparql
SELECT DISTINCT count(?s) as ?nb ?type
WHERE {
  ?s a ?type .
  FILTER (contains(str(?type), "erlangen") || contains(str(?type), "forth"))
}
GROUP BY ?type
ORDER BY DESC(?nb)
```

## Main entities

Pattern:

``` turtle
http://data.silknow.org/<group>/<uuid>
# e.g. http://data.silknow.org/production/d4ec41ba-a4d3-3ebb-ba07-8567f1add9cb/
```

The `<group>` is taken from this table 

| Class | Group |
| --- | --- |
| D1_Digital_Object | object |
| E22_Man-Made_Object | object |
| E73_Information_Object | object |
| E89_Propositional_Object | object |
| E38_Image | image |
| E36_Visual_Item | image | image  |
| E12_Production | production |
| E8_Acquisition | event |
| E10_Transfer_of_Custody | event |
| E9_Move | event |
| E11_Modification | event |
| E78_Collection | collection |
| E14_Condition_Assessment | assessment |
| E53_Place |  place OR http://sws.geonames.org/ {id of place} |
| E31_Document | document |
| E40_Legal_Body | actor |
| E21_Person | actor |
| E39_Actor | actor |

For the metadata predictions (either based on text or image) we created the following two groups, whereas the general pattern is the same as above:

| Class | Group |
| --- | --- |
| prov:Activity | activity |
| rdf:Statement | statement |

## Secondary entities

This group includes entities that cover specific information about the main entities. The URI is realized appending a suffix to the parent main entity.

Pattern if only one instance per main entity is expected:

``` turtle
<uri of the main entity>/<suffix>
# i.e. http://data.silknow.org/object/0359045f-21e1-3488-8bfc-5620ab7ea6d7/dimension/measurement
```

Pattern if multiple instance per main entity are possible:
``` turtle
<uri of the main entity>/<suffix>/<progressive int>
# i.e. <http://data.silknow.org/object/0359045f-21e1-3488-8bfc-5620ab7ea6d7/type_assignment/3>
```

The `<suffix>` is taken from this table:

| Class | Group | Suffix |
| --- | --- | --- |
| E17_Type_Assignment | object | type/{progressive int} |
| T19_Object_Domain_Assignment | object | /domain/{progressive int} |
| T35_Object_Type_Assignment | object | /type/{progressive int} |
| E54_Dimension | object | /dimension/{1 or 2} |
| T24_Pattern_Unit | object | /pattern/{1 or 2} |
| S4_Observation | object | observation/{progressive int} |
| E15_Identifier_Assignment | object | id_assignment/{id from E42} |
| E16_Measurement | object | dimension/measurement |
| E3_Condition_State | object | assessment/{progressive int} |
| E30_Right | object | /right/ |
| E30_Right | object | /image/right/ |
| E52_Time-Span | production | time/{progressive int} |
| E7_Activity | production | activity/{progressive int}  |


## UUID and seed generation

The UUID is computed deterministically starting from a seed string. A real UUID taken from an example above looks like this: d4ec41ba-a4d3-3ebb-ba07-8567f1add9cb

The seed is usually generated  based on:

* source (e.g. 'unipa', 'met', ...)
* class (e.g. 'E12_Production', ...)
* the id of the current object (filename or crawler ID, which is often the same)
* Hash function: SHA-1

There are some exceptions to this rule, in order to allow automatic cross-source alignment:
* For D1 Digital Object, the filename (without extension) and the ID of the JSON file are used.
* For Places and Actors, we use the label ('Rome' e.g.) instead of the id plus the class, but not the source.
* For Collection, it's the same case as for Places and Actors, but we also use the source addition.
* For the Images we use a concatenation of id (internal ID of museum record) + "$$$" + imgCount (How many images does the object have) + this.localFilename
* For E11 Modification the seed is either the value of the original field, or (if it can be parsed from it) the author or the year of the modification.
* For rdf:Statement and prov:Activity the UUID is always the same per prediction. The seed is either the according object UUID + the image file name and the predicted class (image-based predictions) or the object UUID + the predicted value + the confidence score + the file name of the prediction csv (text-based).

Examples:
* For most classes: [source]+[class]+[record_internal_id]
* For Place (E53) and Actors (E39): [class]+[label]
* For Collections  (E78): [source]+[class]+[label]
* For Images (E38): [record_internal_id] + "$$$" + [img_count_of_record] + [local_filename]

## D1 Digital Object and rdfs:label

We use the property "rdfs:label" to store a combination of the actual internal identifier of a record and the former filename.

* **Example:**      
*ID_T.125-1992_filename_O65533.json* (VAM), where "T.125-1992" is the internal identifier and "O65533.json" is the filename. "ID", "filename" and three "_" get added to every rdfs:label to make it easier to read.

* **ID:**         
The ID is taken either from the internal museum / dataset ID, if it has a field with such a number. Or a generated ID from the crawling process.
* **Filename:**   
It's the filename of the record before it's conversion, which is usually in the common JSON format into which we pre-process most data. In the case of        Garin we convert Excel files, that have been provided to us by them (*.xls). Eventual language information from the filename (like "en" or "es") is removed.
