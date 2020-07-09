    ./gradlew --stop
    ./gradlew clean
    ./gradlew run --args="imatex ../crawler/data/imatex -o ./output/imatex -g tschleider"
    ./gradlew run --args="joconde ../crawler/data/joconde -o ./output/joconde -g tschleider"
    ./gradlew run --args="garin '../crawler/data/garin' -o ./output/garin -g tschleider"
    ./gradlew run --args="VAM '../crawler/data/vam' -o ./output/vam -g tschleider"
    ./gradlew run --args="MFA '../crawler/data/mfa-boston' -o ./output/mfa -g tschleider"
    ./gradlew run --args="MAD '../crawler/data/les-arts-decoratifs' -o ./output/mad -g tschleider"
    ./gradlew run --args="MET '../crawler/data/met-museum' -o ./output/met -g tschleider"
    ./gradlew run --args="RISD '../crawler/data/risd-museum' -o ./output/risd -g tschleider"
    ./gradlew run --args="UNIPA '../crawler/data/unipa' -o ./output/unipa -g tschleider"
    ./gradlew run --args="CER '../crawler/data/ceres-mcu' -o ./output/cer -g tschleider"
    ./gradlew run --args="MTMAD '../crawler/data/mtmad' -o ./output/mtmad -g tschleider"


