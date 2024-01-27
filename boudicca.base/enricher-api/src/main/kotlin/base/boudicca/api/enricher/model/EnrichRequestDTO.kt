package base.boudicca.api.enricher.model

import base.boudicca.model.Event

data class EnrichRequestDTO(
    val events: List<Event>?,
)