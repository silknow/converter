python prov_o_place.py
cd place
cat *.ttl > image_place.ttl
mv image_place.ttl ../
python prov_o_material.py
cd ../material
cat *.ttl > image_material.ttl
mv image_material.ttl ../
python prov_o_time.py
cd ../time
cat *.ttl > image_time.ttl
mv image_time.ttl ../
python prov_o_technique.py
cd ../technique
cat *.ttl > image_technique.ttl
mv image_technique.ttl ../
cd ../
rm *.gz
gzip *.ttl
