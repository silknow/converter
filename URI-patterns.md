URI patterns for SILKNOW data
==============================

This file documents how the URI are generated for the SILKNOW data.
See also [#42](https://github.com/silknow/converter/issues/42).


## Main entities

Pattern:

``` turtle
http://data.silknow.org/<group>/<uuid>
# i.e. http://data.silknow.org/production/d4ec41ba-a4d3-3ebb-ba07-8567f1add9cb/activity/1
```

The `<group>` is taken from this table 

| Class | Group | Count
| --- | --- | --- |
| E38_Image | image | 65050 |
| D1_Digital_Object | - | 35137 |
| E22_Man-Made_Object | object | 35126 |
| E12_Production | production | 35126 | 
| E8_Acquisition | event | 32945 |
| E10_Transfer_of_Custody | event | 32067 |
| E78_Collection | collection | 18827 |
| E73_Information_Object | informationobject | 3331 |
| E14_Condition_Assessment | assessment | 3180 |
| E9_Move | event | 3059 |
| E53_Place |  place OR http://sws.geonames.org/ {id of place} | 2466 |
| E39_Actor | actor | 824 |
| E11_Modification | modification | 751 |
| E89_Propositional_Object | object | 750 |
| E31_Document | document | 375 
| E40_Legal_Body | organization | 33
| E21_Person | person | 2

Numbers taken from this [query](https://data.silknow.org/sparql?default-graph-uri=&query=SELECT+count%28%3Fs%29+as+%3Fcount+%3FclassORproperty%0D%0AWHERE+%7B+graph+%3Fg+%7B+%0D%0A++++++%3Fs+a+%3FclassORproperty%0D%0A%0D%0A%0D%0AFILTER+%28contains%28str%28%3Fg%29%2C+%22mad%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22mtmad%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22joconde%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22imatex%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22unipa%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22risd%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22vam%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22met%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22cer%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22garin%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22mfa%22%29%29%0D%0A%0D%0A%7D%7D%0D%0A%0D%0AGROUP+BY+%3FclassORproperty%0D%0AORDER+BY+DESC+%28%3Fcount%29&should-sponge=&format=text%2Fhtml&timeout=0&debug=on&run=+Run+Query+)

## Secondary entities

This group includes entities that cover specific information about the main entities.
The URI is realized appending a suffix to the parent main entity.

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

| Class | Group | Suffix | Count
| --- | --- | --- | --- |
| E17_Type_Assignment | object | type_assignment/{progressive int} | 52685 |
| E54_Dimension | object | dimension/{w or h} | 45522 |
| E42_Identifier | object | id/{id} | 35872 |
| S4_Observation | object | observation/{progressive int} | 34138 |
| E15_Identifier_Assignment | object | id_assignment/{id from E42} | 35498 |
| E52_Time-Span | production | time/{progressive int} | 25415 |
| E16_Measurement | object | dimension/measurement | 22781 |
| E3_Condition_State | object | assessment/{progressive int} | 9298 |
| E7_Activity | production | activity/{progressive int}  | 5245 |
| E30_Right | object | right | 750 |

Numbers taken from this [query](https://data.silknow.org/sparql?default-graph-uri=&query=SELECT+count%28%3Fs%29+as+%3Fcount+%3Ft%0D%0AWHERE+%7B+graph+%3Fg+%7B+%0D%0A++++++%3Fs+a+%3Ft%0D%0A%0D%0A%0D%0AFILTER+%28contains%28str%28%3Fg%29%2C+%22mad%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22mtmad%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22joconde%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22imatex%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22unipa%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22risd%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22vam%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22met%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22cer%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22garin%22%29+%7C%7C+contains%28str%28%3Fg%29%2C+%22mfa%22%29%29%0D%0A%0D%0A%7D%7D%0D%0A%0D%0AGROUP+BY+%3Ft+%0D%0AORDER+BY+DESC+%28%3Fcount%29&format=text%2Fhtml&timeout=0&debug=on)


## UUID and seed generation

The UUID is computed deterministically starting from a seed string.
A real UUID taken from an example above looks like this: d4ec41ba-a4d3-3ebb-ba07-8567f1add9cb

The seed is usually generated  based on:

* source (e.g. 'unipa', 'met', ...)
* class (e.g. 'E22_Man-Made_Object', 'D1_Digital_Object', ...)
* the id of the current object (normally it is unique in a file)
* Hash function: SHA-1

There are some exceptions to this rule, in order to allow automatic cross-source alignment:
* For Places and Actors, we use the label ('Rome' e.g.) instead of the id plus the class, but not the source.
* For Collection, it's the same case as for Places and Actors, but we also use the source addition.
* For the Images we use a concatenation of id (internal ID of museum record) + "$$$" + imgCount (How many images does the object have) + this.localFilename

Examples:
* For most classes: [source]+[class]+[record_internal_id]
* For Place (E53) and Actors (E39): [class]+[label]
* For Collections  (E78): [source]+[class]+[label]
* For Images (E38): [record_internal_id] + "$$$" + [img_count_of_record] + [local_filename]
