package events.boudicca.enricher.service

import events.boudicca.enricher.model.EnrichRequestDTO
import events.boudicca.enricher.model.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EnricherService @Autowired constructor(
    private val enrichers: List<Enricher>
) {

    fun enrich(enrichRequestDTO: EnrichRequestDTO): List<Event> {
        return (enrichRequestDTO.events ?: emptyList()).map {
            var enrichedEvent = it
            for (enricher in enrichers) {
                enrichedEvent = enricher.enrich(enrichedEvent)
            }
            enrichedEvent
        }
    }

}
