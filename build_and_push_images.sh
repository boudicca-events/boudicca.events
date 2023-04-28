#!/bin/sh

./gradlew imageBuild

#quarkus is stupid and does not allow me to make simple names
docker tag registry-1.docker.io/library/boudicca-api registry.slothyx.com/boudicca-api
docker tag boudicca-html registry.slothyx.com/boudicca-html
docker tag registry-1.docker.io/library/boudicca-ical registry.slothyx.com/boudicca-ical
docker tag boudicca-eventcollectors registry.slothyx.com/boudicca-eventcollectors

docker push registry.slothyx.com/boudicca-api
docker push registry.slothyx.com/boudicca-html
docker push registry.slothyx.com/boudicca-ical
docker push registry.slothyx.com/boudicca-eventcollectors
