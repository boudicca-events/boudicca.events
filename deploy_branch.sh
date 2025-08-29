#!/bin/sh

branch="$1"
cleanedBranch=`echo $branch | sed 's/[^a-zA-Z]*//g' | tr '[:upper:]' '[:lower:]'`;

imageNamePublisher="ghcr.io/boudicca-events/publisher-event-html:branchdeployer-$cleanedBranch"
echo "pushing $imageNamePublisher"
docker tag localhost/boudicca-events-publisher-event-html "$imageNamePublisher"
docker push "$imageNamePublisher"

imageNameSearch="ghcr.io/boudicca-events/search:branchdeployer-$cleanedBranch"
echo "pushing $imageNameSearch"
docker tag localhost/boudicca-search "$imageNameSearch"
docker push "$imageNameSearch"

echo "imageNamePublisher=$imageNamePublisher" >> "$GITHUB_OUTPUT"
echo "imageNameSearch=$imageNameSearch" >> "$GITHUB_OUTPUT"