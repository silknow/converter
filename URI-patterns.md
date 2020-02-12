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
| E38_Image | image | 64811 |
| E22_Man-Made_Object | object | 34653 |
| D1_Digital_Object | - | 34595 |
| E12_Production | production | 34519 | 
| E8_Acquisition | event | 32309 |
| E10_Transfer_of_Custody | event | 31418 |
| E78_Collection | collection | 18827 |
| E73_Information_Object | informationobject | 3331 |
| E14_Condition_Assessment | assessment | 3219 |
| E9_Move | event | 3101 |
| E53_Place |  place OR http://sws.geonames.org/ {id of place} | 2481 |
| E39_Actor | actor | 824 |
| E11_Modification | modification | 753 |
| E89_Propositional_Object | object | 752 |
| E31_Document | document | 376 
| E40_Legal_Body | organization | 33
| E21_Person | person | 2

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

| Class | Suffix | Count
| --- | --- | --- |
| E17_Type_Assignment | /object/{uri of main entity}/type_assignment/{progressive int} | 53379 |
| E54_Dimension | /object/{uri of main entity}/dimension/{w or h} | 44800 |
| E42_Identifier | /object/{uri of main entity}/id/{id} | 35263 |
| E15_Identifier_Assignment | /object/{uri of main entity}/id_assignment/{id from E42} | 34889 |
| S4_Observation | /object/{uri of main entity}/observation/{progressive int} | 32641 |
| E52_Time-Span | /production/{uri of main entity}/time/{progressive int} | 32295 |
| E16_Measurement | /object/{uri of main entity}/dimension/measurement | 22400 |
| E3_Condition_State | /object/{uri of main entity}/assessment/{progressive int} | 9421 |
| E7_Activity | /production/{uri of main entity}/activity/{progressive int}  | 5220 |
| E30_Right | /object/{uri of main entity}/right/ | 752 |
