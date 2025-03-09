# Release Checklist

1. Create a release-branch and bump versions
   1. Create a release branch in the format of `release/<major>.<minor>` like for example `release/0.7`
   2. Set the version in version.txt on the release branch to the actual version (aka delete the -SNAPSHOT)
   3. Increase dev-Version in version.txt on the main branch
2. Build client libraries release.zip
   1. Locally, in some Linux-env, checkout the newly created commit on the release branch
   2. Setup signing key properties (instructions found in the keypass file in the descriptions of "Publishing GPG Key")
   3. run ` ./create_release_zip.sh <signing password>` where signing password is found in "Publishing GPG Key". (not the space in the beginning of the command, this prevents bash from saving the command and the password in its history)
3. Upload the created release.zip onto https://central.sonatype.com/
   1. Go to the website and login with the "Maven Central" credentials from the keepass file
   2. Click the email on the upper right and go to "View Deployments"
   3. Click "Publish Component"
   4. Enter the name of the deployment in the form of "boudicca <version>"
   5. Upload the generated release.zip
   6. Wait until all checks are done, status should go from "Pending" to "Validated" (should not take long, but you need to refresh the page manually)
   7. Make a quick check if the version is the right one and the components listed seem correct and there are no other obvious errors
   8. Click "Publish"
   9. Go drink a coffee, because the actual publishing takes up to 30 minutes... but we are done here anyway, you can continue with the next steps
4. Publish Docker-images (FOR THE CORRECT BRANCH) to docker.io by running https://github.com/boudicca-events/boudicca.events/actions/workflows/publish.yml
5. Check all samples and docs if they need updating, especially this list:
   * [REST.md](../REST.md)
   * https://github.com/boudicca-events/remote-collector-samples
   * https://github.com/boudicca-events/openapi-generate-sample
   * https://github.com/boudicca-events/client-samples
6. You are done, good job!