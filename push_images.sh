#!/bin/sh

docker tag localhost/boudicca-eventdb registry.slothyx.com/boudicca-eventdb
docker tag localhost/boudicca-enricher registry.slothyx.com/boudicca-enricher
docker tag localhost/boudicca-search registry.slothyx.com/boudicca-search
docker tag localhost/boudicca-html registry.slothyx.com/boudicca-html
docker tag localhost/boudicca-ical registry.slothyx.com/boudicca-ical
docker tag localhost/boudicca-eventcollectors registry.slothyx.com/boudicca-eventcollectors

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
