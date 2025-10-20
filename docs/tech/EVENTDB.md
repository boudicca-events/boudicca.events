# EventDB

The EventDB has multiple jobs/apis, including accepting new Entries/data, persisting them, cleaning up old Entries and then providing an interface for others to actually get them.

It is a simple SpringBoot app you can find [here](../../boudicca.base/eventdb)

## Ingestion API

This API is responsible for ingesting new Entries, it is implemented as a Spring RestController you can find it [here](../../boudicca.base/eventdb/src/main/kotlin/base/boudicca/entrydb/controller/IngestionController.kt)

This is the only API currently secured by a password in the whole Boudicca Platform.

## Publisher API

This API is responsible for publishing all available Entries, it is also implemented as a Spring RestController you can find it [here](../../boudicca.base/eventdb/src/main/kotlin/base/boudicca/entrydb/controller/PublisherController.kt)

Currently, this only has a "get all" method, which maybe will be extended in the future.

## EntryService

This is the heart of the EventDB, responsible for persisting entries and cleaning them up after a while.
The main storage is a simple ConcurrentHashMap where all operations will be executed on. Additionally, the EventDB saves all data to disk in a JSON format periodically and loads it on startup.

The current mechanism for cleaning old Entries needs improvement. 
It looks for each EventCollector when they last submitted a new Entry, and on cleanup if an Entry was not updated by a EventCollector for three days, but others were, then it will be removed.
This of course relies on the EventCollector always sending a full collection, and not only changes/new events.
