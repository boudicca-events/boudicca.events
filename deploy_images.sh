#!/bin/sh

docker tag localhost/boudicca-eventdb ghcr.io/boudicca-events/eventdb
docker tag localhost/boudicca-enricher ghcr.io/boudicca-events/enricher
docker tag localhost/boudicca-search ghcr.io/boudicca-events/search
docker tag localhost/boudicca-events-publisher-event-html ghcr.io/boudicca-events/publisher-event-html
docker tag localhost/boudicca-publisher-event-ical ghcr.io/boudicca-events/publisher-event-ical
docker tag localhost/boudicca-events-eventcollectors ghcr.io/boudicca-events/eventcollectors

echo "pushing eventdb"
docker push ghcr.io/boudicca-events/eventdb
echo "pushing enricher"
docker push ghcr.io/boudicca-events/enricher
echo "pushing search"
docker push ghcr.io/boudicca-events/search
echo "pushing html"
docker push ghcr.io/boudicca-events/publisher-event-html
echo "pushing ical"
docker push ghcr.io/boudicca-events/publisher-event-ical
echo "pushing eventcollectors"
docker push ghcr.io/boudicca-events/eventcollectors
