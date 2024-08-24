# Enricher

Enricher services can be called by EventCollectors to add additional data to events, a process we call "enrich".
This can be useful for allowing multiple EventCollectors to use common data or processing.  

There can exist multiple Enricher services, and each of those can contain multiple actual Enrichers.
Currently, we have one Enricher service with multiple Enrichers as a simple SpringBoot app you can find [here](../../boudicca.base/enricher)

## Enricher API

This API offers a single endpoint for submitting a list of events and get a list of enriched events back. It is implemented as a Spring RestController you can find it [here](../../boudicca.base/enricher/src/main/kotlin/base/boudicca/enricher/controller/EnricherController.kt)

## EnricherService

This service will call the actual Enrichers in the specified order and contains some error handling as well.
If the order of the actual Enrichers is important, for example if one depends on the other, use the SpringBoot `@Order` mechanism

## Enricher

Actual Enrichers implement the [Enricher](../../boudicca.base/enricher/src/main/kotlin/base/boudicca/enricher/service/Enricher.kt) interface and will be called by the EnricherService.

Some guidelines to implementing Enrichers:
* preserve existing data
  * if an event already has some data set from the EventCollector the Enricher should preserve this data

Current implementations are:

### CategoryEnricher

If no category is set this Enricher will try to look up the category via the type property of the event. Currently, this lookup is hardcoded via the [EventCategory](../../boudicca.base/semantic-conventions/src/main/kotlin/base/boudicca/model/EventCategory.kt) enum

### RecurrenceEnricher

The recurrence enricher has a look at how often Events with the same name are collected and if they count more than 3 Events with the same name it will mark all of them as recurring.
This surely can be made better.

### LocationEnricher

The LocationEnricher has a look at the `location.name` and `location.address` property of an Event and sees if it has additional data for that.
If some additional data is found it will apply all of them to the Event.

Currently, this additional data is maintained via a Google Sheet: https://docs.google.com/spreadsheets/d/1yYOE5gRR6gjNBim7hwEe3__fXoRAMtREkYbs-lsn7uM/edit#gid=1401733047 and is updated every hour.
If you want to contribute to this data please contact us via our email or our discord server.

### MusicBrainzArtistEnricher

This enricher looks at the name of an Event with category `MUSIC` and searches for known Artist names in there. If it finds them it will set the `concert.genre` and `concert.bandlist` properties.
This enricher depends on the CategoryEnricher to run first.
The data is manually loaded from https://data.metabrainz.org/pub/musicbrainz/data/json-dumps/ and processed manually via [MusicBrainzImporter.kt](../../boudicca.base/enricher-utils/src/main/kotlin/base/boudicca/enricher_utils/MusicBrainzImporter.kt)
