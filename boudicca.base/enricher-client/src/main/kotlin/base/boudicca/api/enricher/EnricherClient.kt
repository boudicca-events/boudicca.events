package base.boudicca.api.enricher

import base.boudicca.model.Event

interface EnricherClient {
    fun enrichEvents(events: List<Event>): List<Event>
}
