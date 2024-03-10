package base.boudicca.api.eventcollector.runner

import base.boudicca.model.Event

object NoopRunnerEnricherInterface : RunnerEnricherInterface {
    override fun enrichEvents(events: List<Event>): List<Event> {
        return events
    }
}