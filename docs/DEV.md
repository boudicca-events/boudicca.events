# Developing

## Local Setup

We use Intellij Idea for which launch configs are found in the [.run](../.run) folder.
You should be able to use other IDEs since our build is a simple multiproject Gradle build.
Please note that we require Java 21 to run.

### First step

After checkout please build the whole project with `gradlew build` to generate all openapi generated classes.

### Frontend only setup

If you only want to develop the frontend ([publisher-event-html](../boudicca.base/publisher-event-html)) then you can
use the `OnlineHtmlPublisher` run config. This will start the frontend which uses the online boudicca.events website as a backend.

Both the `OnlineHtmlPublisher` and the `LocalHtmlPublisher` launch configurations set the
`boudicca.devMode=true` property which will circumvent caching of local resources, so if you only change templates or
static resources like Javascript or CSS, a simple reload will fetch your changes, and you do not need to restart or
rebuild the server. For changes in Kotlin classes you will need to trigger a recompile (Ctrl+F9 in Intellij), which
should also automatically restart the server.

### Full setup

Run the compound launch config `Local Setup (without collectors)` which runs `LocalEventDB`,`LocalSearch`
and `LocalHtmlPublisher` (aka the frontend) all at once.

Then you can fill up the database with one of two options:

- (recommended) Run `LocalFetchFromOnlineBoudiccaKt` to fetch the events from the online boudicca.events website.
- Run some EventCollector locally, for example for testing a newly created EventCollector, for more information
  see [Developing your own Collector](#developing-your-own-collector)

After the database is filled up, call http://localhost:8080 to see the application.

The local EventDB saves its data into the file `boudicca.store` in the root folder of the project. So if you want to
clean our EventDB, stop it, delete the file and restart it.

## Developing your own Collector

see [here](tech/EVENTCOLLECTORS.md#developing-your-own-collector)