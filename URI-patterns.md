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

| Class | group |
| --- | --- |
| E38_Image | image |
| E17_Type_Assignment | object |
| E54_Dimension | object |
| E42_Identifier | object |
| E15_Identifier_Assignment | object |
| E22_Man-Made_Object | object |
| D1_Digital_Object | - |
| E12_Production | production |
| S4_Observation |  |
| E8_Acquisition |  |
| E10_Transfer_of_Custody |  |
| E16_Measurement |  |
| E52_Time-Span |  |
| E78_Collection |  |
| E3_Condition_State |  |
| E7_Activity |  |
| E73_Information_Object |  |
| E9_Move |  |
| E53_Place |  |
| E39_Actor |  |
| E11_Modification |  |
| E89_Propositional_Object |  |
| E30_Right |  |
| E31_Document |  |
| E40_Legal_Body |  |
| E21_Person |  |

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
