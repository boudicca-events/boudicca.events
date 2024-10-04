#!/bin/bash

#delete old releases
rm -rf build/release_repo
rm release.zip

set -e

#clean
./gradlew clean

#build new release
./gradlew publish -DdoSign=true -Psigning.password="$1"

#remove maven repo files
find build/release_repo -name "maven-metadata.*" -delete

#zip it
cd build/release_repo/
zip -r ../../release.zip *
cd ../..
