# EventCollectors

`EventCollector` is a loose name for everything that collects Event data.

But this document is about the actual implementation we have in Boudicca, which you can find here for
the [base classes](../../boudicca.base/eventcollector-client) and here for
the [boudicca.events collectors](../../boudicca.events/eventcollectors)

### How an EventCollectors works

All EventCollectors are subclasses of the
interface [EventCollector](../../boudicca.base/eventcollector-client/src/main/kotlin/base/boudicca/api/eventcollector/EventCollector.kt).
In its simplest form the interface has only two methods you need to implement.

1. getName() which returns a simple name for the collector. The convention is all lowercase without any special
   characters nor spaces.
2. collectEvents() which returns a List
   of [Events](../../boudicca.base/common-model/src/main/kotlin/base/boudicca/model/Event.kt)

and this is all you need for a EventCollector!

### Helper classes

But of course a lot of functionality is common to multiple EventCollectors, so we provide a lot of existing
functionality to help you.

#### TwoStepEventCollector

The [TwoStepEventCollector.kt](../../boudicca.base/eventcollector-client/src/main/kotlin/base/boudicca/api/eventcollector/TwoStepEventCollector.kt)
splits the collection work up into two steps:

1. getAllUnparsedEvents() which collects and returns a list of "unparsed events", whatever this is. This could for
   example be a list of URLs where the detailed event data is found, or html snippets from a site containing all needed
   information.
2. parseMultipleEvents() or parseEvent(), which will be called for each item returned in the first step. These methods
   allow you to return multiple or a single event for one input.

One big advantage of the TwoStepCollector is automatic failure handling if a parseMultipleEvents() or parseEvent()
invocation throws an exception, meaning that other events will still be processed. This also means you should do as
little as possible in the getAllUnparsedEvents() method.

#### Fetcher

The [Fetcher.kt](../../boudicca.base/fetcher-lib/src/main/kotlin/base/boudicca/fetcher/Fetcher.kt)
utility class allows you to easily fetch websites via http(s). It is highly recommended that you use this fetcher
because it also provides automatic delays so that we do not overwhelm the server we scrape, and it also sets
the User-Agent (from global config `Constants.USER_AGENT` currently: `boudicca.events.collector/1.0 (https://boudicca.events/)`).

You should create a new instance of the fetcher via the `FetcherFactory.newFetcher(..)` method.

#### Collections diagnostic data

TODO

## Developing your own Collector

To do this the workflow is following:

1. In
   our [eventcollectors project](../../boudicca.events/eventcollectors/src/main/kotlin/events/boudicca/eventcollector/collectors)
   create a new EventCollector subclass which collects some events.
    1. A good starting point is always to look at existing EventCollectors and copy one of them.
    2. Take a look at our [Semantic Conventions](../SEMANTIC_CONVENTIONS.md) overview where you can see the existing
       keys and their meanings you should adhere to.
2. Add your new collector in [LocalCollectorDebug.kt](../../boudicca.events/eventcollectors/src/main/kotlin/events/boudicca/eventcollector/LocalCollectorDebug.kt)
   and run the `LocalCollectorDebugKt` launch config (or class) to dry-run your test.
    1. A dry-run means events will be collected but not ingested somewhere. This is to make sure that the data looks
       sane and the EventCollector does not throw any exceptions before actually sending it to a backend.
    2. The `LocalCollectorDebugKt` also starts the collectors overview at http://localhost:8083 where you can have an
       easier look at errors and what happened during your collection.
    3. You can also test the collection with enrichment by starting your own local enricher or using our hosted one
       at https://enricher.boudicca.events. For that uncomment one of the `.enableEnricher("...")` lines in
       the [LocalCollectorDebugKt](../../boudicca.events/eventcollectors/src/main/kotlin/events/boudicca/eventcollector/LocalCollectorDebug.kt)   class
3. After your dry-run was successful it is time to test it for real.
    1. Enable the `.enableIngestion()` line in
       the [LocalCollectorDebug.kt](../../boudicca.events/eventcollectors/src/main/kotlin/events/boudicca/eventcollector/LocalCollectorDebug.kt).
       This will ingest the collected data into the local EventDB.
    2. Then start your local [Full Setup](../DEV.md#full-setup)
    3. Run the `LocalCollectorDebugKt` launch config and follow the collection progress at http://localhost:8083
    4. After the collection is done have a look at http://localhost:8080 and see if everything works as designed (names,
       dates,
       pictures, links, ...)
4. Lastly, add your new EventCollector to the
   class [BoudiccaEventCollectors.kt](../../boudicca.events/eventcollectors/src/main/kotlin/events/boudicca/eventcollector/BoudiccaEventCollectors.kt)
   so that it will be used after deployment and create your Pull Request :)

Note: The [LocalCollectorDebugKt](../../boudicca.events/eventcollectors/src/main/kotlin/events/boudicca/eventcollector/LocalCollectorDebug.kt) caches HTTP calls done via the
Fetcher in a file called `fetcher.cache` to speed up testing and reduce traffic to external servers. You can just delete the file to clean the cache and re-fetch the website.
