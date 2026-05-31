package base.boudicca.api.eventdb.publisher

import base.boudicca.model.Entry
import base.boudicca.model.Event

interface EventDbPublisherClient {
    fun getAllEvents(): Set<Event>
    fun getAllEntries(): Set<Entry>
}
