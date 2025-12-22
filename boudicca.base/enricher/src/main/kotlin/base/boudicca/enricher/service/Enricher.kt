package base.boudicca.enricher.service

import base.boudicca.model.structured.StructuredEvent

interface Enricher {
    fun enrich(event: StructuredEvent): StructuredEvent = throw NotImplementedError("enricher has not implement list or single enrich operation")

    fun enrich(events: List<StructuredEvent>): List<StructuredEvent> = events.map { enrich(it) }
}
