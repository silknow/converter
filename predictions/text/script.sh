python prov_o_place.py
cd place
cat *.ttl > text_place.ttl
mv text_place.ttl ../
python prov_o_material.py
cd ../material
cat *.ttl > text_material.ttl
mv text_material.ttl ../
python prov_o_time.py
cd ../time
cat *.ttl > text_time.ttl
mv text_time.ttl ../
python prov_o_technique.py
cd ../technique
cat *.ttl > text_technique.ttl
mv text_technique.ttl ../
cd ../
rm *.gz
gzip *.ttl
