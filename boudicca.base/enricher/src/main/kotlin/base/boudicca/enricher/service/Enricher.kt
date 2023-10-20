package base.boudicca.enricher.service

import base.boudicca.enricher.model.Event


interface Enricher {
    fun enrich(e: Event): Event
}
