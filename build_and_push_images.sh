#!/bin/sh

./gradlew imageBuild

docker tag boudicca-events-boudicca-html-diabolical registry.slothyx.com/boudicca-html-diabolical

echo "pushing diabolical"
docker push registry.slothyx.com/boudicca-html-diabolical
