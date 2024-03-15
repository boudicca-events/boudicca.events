package base.boudicca.enricher.service

import base.boudicca.model.Event


interface Enricher {
    fun enrich(e: Event): Event {
        throw NotImplementedError("enricher has not implement list or single enrich operation")
    }

    fun enrich(events: List<Event>): List<Event> {
        return events.map { enrich(it) }
    }
}
