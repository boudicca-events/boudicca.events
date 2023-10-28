package base.boudicca.enricher.service

import base.boudicca.model.Event


interface Enricher {
    fun enrich(e: Event): Event
}
