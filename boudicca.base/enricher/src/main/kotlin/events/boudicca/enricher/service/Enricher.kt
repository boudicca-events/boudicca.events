package events.boudicca.enricher.service

import events.boudicca.enricher.model.Event


interface Enricher {
    fun enrich(e: Event): Event
}
