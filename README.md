# SILKNOW converter
SILKNOW converter that harmonizes all museum metadata records into the common SILKNOW ontology model (based on CIDOC-CRM)


## Requirements
- Java >1.8

## How to use


    ./gradlew run --args="[--log=<logLevel>] [-o=<outputFolder>] TYPE FOLDER"

Positional arguments (required):    
- `TYPE`. Type of source data: imatex, garin, joconde
- `FOLDER`. Source folder to process. Also a single file is accepted.
- `-g`, `--geonames`. Geonames username. You can take your at http://www.geonames.org/login.
Optional parameters:

- `--log`.   The log level, according [SLF4J nomenclature](https://www.slf4j.org/api/org/apache/commons/logging/Log.html). Default: `WARN`.
- `-o`, `--output`.  Output folder. Default: an `out` folder siblings to the
                           input directory.


Examples:
   
    ./gradlew run --args="imatex ../crawler/data/imatex/records/3345_en.json --log DEBUG -o ./output"
    ./gradlew run --args="imatex ../crawler/data/imatex/records/3008_en.json --log DEBUG -o ./output"
    ./gradlew run --args="joconde ../crawler/data/joconde/records/95.71.2.json -o ./output"
    ./gradlew run --args="joconde ../crawler/data/joconde/records/95.71.364.json -o ./output"
    ./gradlew run --args="garin '../../Desktop/garin/Tejidos/Tejidos A/T000053.xls' -o ./output"
    ./gradlew run --args="MET '../crawler/data/met-museum/records/08.48.46.json' -o ./output"
  
Full conversion:

    ./gradlew run --args="imatex ../crawler/data/imatex/records/ -o ./output/imatex -g <GEONAMES_USER>"
    ./gradlew run --args="joconde ../crawler/data/joconde/records/ -o ./output/joconde -g <GEONAMES_USER>"
    ./gradlew run --args="garin '../../Desktop/garin/' -o ./output/garin -g <GEONAMES_USER>"
