# Deployment

TODO add docu

## Recreate Nominatim Database

1) go to /opt/boudicca/
2) download newest version of data from https://download.geofabrik.de/europe/austria-latest.osm.pbf
3) run docker command below (REPLACE the password)

or as script:

cd /opt/boudicca/
wget https://download.geofabrik.de/europe/austria-latest.osm.pbf
docker run -it \
-e PBF_PATH=/atdata/austria-latest.osm.pbf \
-e FREEZE=true \
-e IMPORT_WIKIPEDIA=true \
-e NOMINATIM_PASSWORD=<GET_PASSWORD_FROM_KEEPASS> \
-e IMPORT_STYLE=extratags \
-v /opt/boudicca/austria-latest.osm.pbf:/atdata/austria-latest.osm.pbf \
-v nominatim-data:/var/lib/postgresql/16/main \
-p 8080:8080 \
--rm
mediagis/nominatim:4.5
