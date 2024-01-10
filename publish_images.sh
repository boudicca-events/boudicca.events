#!/bin/sh

docker tag localhost/boudicca-eventdb docker.io/boudicca/eventdb
docker tag localhost/boudicca-enricher docker.io/boudicca/enricher
docker tag localhost/boudicca-search docker.io/boudicca/search
docker tag localhost/boudicca-publisher-event-html docker.io/boudicca/publisher-event-html
docker tag localhost/boudicca-publisher-event-ical docker.io/boudicca/publisher-event-ical

echo "publishing eventdb"
docker push docker.io/boudicca/eventdb
echo "publishing enricher"
docker push docker.io/boudicca/enricher
echo "publishing search"
docker push docker.io/boudicca/search
echo "publishing publisher-event-html"
docker push docker.io/boudicca/publisher-event-html
echo "publishing publisher-event-ical"
docker push docker.io/boudicca/publisher-event-ical
