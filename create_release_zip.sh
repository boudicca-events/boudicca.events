#!/bin/bash

#delete old releases
rm -rf build/release_repo
rm release.zip

set -e

#build new release
./gradlew publish -DdoSign=true

#remove maven repo files
find build/release_repo -name "maven-metadata.*" -delete

#zip it
zip -r release.zip build/release_repo/*
