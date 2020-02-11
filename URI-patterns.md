URI patterns for SILKNOW data
==============================

This file documents how the URI are generated for the SILKNOW data.
See also [#42](https://github.com/silknow/converter/issues/42).


## Main entities

Pattern:

``` turtle
http://data.silknow.org/<group>/<uuid>
# i.e. <http://data.silknow.org/production/383e4b9d-3299-3e1a-aab0-d0b08f0e67e3>
```

The `<group>` is taken from this table 

| Class | Group | Count
| --- | --- | --- |
| E38_Image | image | 64811 |
| E17_Type_Assignment | object | 53379 |
| E54_Dimension | object | 44800 |
| E42_Identifier | object | 35263 |
| E15_Identifier_Assignment | object | 34889 |
| E22_Man-Made_Object | object | 34653 |
| D1_Digital_Object | - | 34595 |
| E12_Production | production | 34519 | 
| S4_Observation |  | 32641 |
| E8_Acquisition |  | 32309 |
| E10_Transfer_of_Custody |  | 31418 |
| E16_Measurement |  | 22400 |
| E52_Time-Span |  | 32295 |
| E78_Collection |  | 18827 |
| E3_Condition_State |  | 3219 |
| E7_Activity |  | 5220 |
| E73_Information_Object |  | 3331 |
| E9_Move |  | 3101 |
| E53_Place |  | 2481 |
| E39_Actor |  | 824 |
| E11_Modification |  | 753 |
| E89_Propositional_Object |  | 752 |
| E30_Right |  | 752 |
| E31_Document |  | 376 
| E40_Legal_Body |  | 33
| E21_Person |  | 2

## Secondary entities

This group includes entities that cover specific information about the main entities.
The URI is realized appending a suffix to the parent main entity.

Pattern if only one instance per main entity is expected:

``` turtle
<uri of the main entity>/<suffix>
# i.e. http://data.doremus.org/expression/ad8ddf1f-f1d1-3284-91d7-34fe655f8258/dedication
```

Pattern if multiple instance per main entity are possible:
``` turtle
<uri of the main entity>/<suffix>/<progressive int>
# i.e. http://data.doremus.org/expression/6ad3a47e-61a2-3790-8fe1-8bb2a17a3c12/casting/1
```

The `<suffix>` is taken from this table:

| Class | suffix |
| --- | --- |
| M1----| catalog |
| M2--- | opus |
| M4--- | key |
| M6----- | casting |
| M15----- | dedication |
| E4_Period | period |
| E7_Activity | activity |
| E52_Time-Span | interval |
