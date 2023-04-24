#!/bin/sh

./gradlew imageBuild

#quarkus is stupid and does not allow me to make simple names
docker tag registry-1.docker.io/library/boudicca-api registry.slothyx.com/boudicca-api
docker tag boudicca-html registry.slothyx.com/boudicca-html
docker tag registry-1.docker.io/library/boudicca-ical registry.slothyx.com/boudicca-ical
docker tag registry-1.docker.io/library/boudicca-jku registry.slothyx.com/boudicca-jku
docker tag registry-1.docker.io/library/boudicca-technologieplauscherl registry.slothyx.com/boudicca-technologieplauscherl
docker tag registry-1.docker.io/library/boudicca-posthof registry.slothyx.com/boudicca-posthof

docker push registry.slothyx.com/boudicca-api
docker push registry.slothyx.com/boudicca-html
docker push registry.slothyx.com/boudicca-ical
docker push registry.slothyx.com/boudicca-jku
docker push registry.slothyx.com/boudicca-technologieplauscherl
docker push registry.slothyx.com/boudicca-posthof
