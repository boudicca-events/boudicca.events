package base.boudicca.enricher.model

import base.boudicca.Event

data class EnrichRequestDTO(
    val events: List<Event>?,
)