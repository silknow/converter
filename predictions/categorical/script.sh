python prov_o_place.py
cd place
cat *.ttl > xgboost_place.ttl
mv xgboost_place.ttl ../
cd ..
python prov_o_material.py
cd material
cat *.ttl > xgboost_material.ttl
mv xgboost_material.ttl ../
cd ..
python prov_o_time.py
cd time
cat *.ttl > xgboost_time.ttl
mv xgboost_time.ttl ../
cd ..
python prov_o_technique.py
cd technique
cat *.ttl > xgboost_technique.ttl
mv xgboost_technique.ttl ../
cd ..
rm *.gz
gzip *.ttl
