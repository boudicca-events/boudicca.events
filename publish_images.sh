#!/bin/bash

publish () {
  VERSION="$1"
  docker tag localhost/boudicca-eventdb docker.io/boudicca/eventdb:$VERSION
  docker tag localhost/boudicca-enricher docker.io/boudicca/enricher:$VERSION
  docker tag localhost/boudicca-search docker.io/boudicca/search:$VERSION
  docker tag localhost/boudicca-publisher-event-html docker.io/boudicca/publisher-event-html:$VERSION
  docker tag localhost/boudicca-publisher-event-ical docker.io/boudicca/publisher-event-ical:$VERSION

  echo "publishing eventdb:$VERSION"
  docker push docker.io/boudicca/eventdb:$VERSION
  echo "publishing enricher:$VERSION"
  docker push docker.io/boudicca/enricher:$VERSION
  echo "publishing search:$VERSION"
  docker push docker.io/boudicca/search:$VERSION
  echo "publishing publisher-event-html:$VERSION"
  docker push docker.io/boudicca/publisher-event-html:$VERSION
  echo "publishing publisher-event-ical:$VERSION"
  docker push docker.io/boudicca/publisher-event-ical:$VERSION
}

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" == "main" ]]
then
  publish "latest"
fi

VERSION="$(cat version.txt)"
publish "$VERSION"
