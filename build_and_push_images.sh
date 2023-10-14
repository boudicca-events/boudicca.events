#!/bin/sh

./gradlew imageBuild

docker tag boudicca-eventdb registry.slothyx.com/boudicca-eventdb
docker tag boudicca-enricher registry.slothyx.com/boudicca-enricher
docker tag boudicca-search registry.slothyx.com/boudicca-search
docker tag boudicca-html registry.slothyx.com/boudicca-html
docker tag boudicca-ical registry.slothyx.com/boudicca-ical
docker tag boudicca-eventcollectors registry.slothyx.com/boudicca-eventcollectors

echo "pushing eventdb"
docker push registry.slothyx.com/boudicca-eventdb
echo "pushing enricher"
docker push registry.slothyx.com/boudicca-enricher
echo "pushing search"
docker push registry.slothyx.com/boudicca-search
echo "pushing html"
docker push registry.slothyx.com/boudicca-html
echo "pushing ical"
docker push registry.slothyx.com/boudicca-ical
echo "pushing eventcollectors"
docker push registry.slothyx.com/boudicca-eventcollectors
