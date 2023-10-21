package base.boudicca.enricher.service

import base.boudicca.Event


interface Enricher {
    fun enrich(e: Event): Event
}
