package base.boudicca.api.eventdb.ingest

import base.boudicca.model.Entry
import base.boudicca.model.Event

interface EventDbIngestClient {
    fun ingestEvents(events: List<Event>)

    fun ingestEntries(entries: List<Entry>)
}
