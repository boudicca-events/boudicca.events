#!/bin/sh

./gradlew imageBuild

#quarkus is stupid and does not allow me to make simple names
docker tag boudicca-eventdb registry.slothyx.com/boudicca-eventdb
docker tag boudicca-search registry.slothyx.com/boudicca-search
docker tag boudicca-html registry.slothyx.com/boudicca-html
docker tag boudicca-ical registry.slothyx.com/boudicca-ical
docker tag boudicca-eventcollectors registry.slothyx.com/boudicca-eventcollectors

docker push registry.slothyx.com/boudicca-eventdb
docker push registry.slothyx.com/boudicca-search
docker push registry.slothyx.com/boudicca-html
docker push registry.slothyx.com/boudicca-ical
docker push registry.slothyx.com/boudicca-eventcollectors
