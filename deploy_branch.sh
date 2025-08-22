#!/bin/sh

branch="$1"
cleanedBranch=`echo $branch | sed 's/[^a-zA-Z]*//g' | tr '[:upper:]' '[:lower:]'`;
imageName="ghcr.io/boudicca-events/publisher-event-html:branchdeployer-$cleanedBranch"

echo "pushing $imageName"
docker tag localhost/boudicca-events-publisher-event-html "$imageName"
docker push "$imageName"