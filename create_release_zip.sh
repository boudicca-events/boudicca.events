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
cd build/release_repo/
zip -r ../../release.zip *
cd ../..
