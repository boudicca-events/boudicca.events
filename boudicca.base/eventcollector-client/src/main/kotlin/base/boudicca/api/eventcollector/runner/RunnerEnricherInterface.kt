package base.boudicca.api.eventcollector.runner

import base.boudicca.api.enricher.EnricherClient
import base.boudicca.api.eventcollector.Configuration
import base.boudicca.model.Event

fun interface RunnerEnricherInterface {

    companion object {
        fun createFromConfiguration(): RunnerEnricherInterface {
            val enricherUrl = Configuration.getProperty("boudicca.enricher.url")
            if (enricherUrl.isNullOrBlank()) {
                return NoopRunnerEnricherInterface
            }
            val enricher = EnricherClient(enricherUrl)
            return BoudiccaRunnerEnricherInterface(enricher)
        }
    }

    fun enrichEvents(events: List<Event>): List<Event>
}
