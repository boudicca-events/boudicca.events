package boudicca.base.model.enricher

import base.boudicca.model.Event

data class EnrichRequestDTO(
    val events: List<Event>?,
)