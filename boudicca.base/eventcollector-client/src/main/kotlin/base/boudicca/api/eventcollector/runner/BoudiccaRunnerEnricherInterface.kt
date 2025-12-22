package base.boudicca.api.eventcollector.runner

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.model.Event

class BoudiccaRunnerEnricherInterface(
    private val enricherClient: EnricherClient,
) : RunnerEnricherInterface {
    override fun enrichEvents(events: List<Event>): List<Event> = enricherClient.enrichEvents(events)
}
