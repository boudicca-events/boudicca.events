package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectionRunner
import base.boudicca.api.eventcollector.collectors.BoudiccaCollector
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface

fun main() {
    CollectionsFilter.alsoLog = true
    EventCollectionRunner(
        listOf(BoudiccaCollector("https://eventdb.boudicca.events")),
        RunnerIngestionInterface.createFromConfiguration(),
        RunnerEnricherInterface.createFromConfiguration(),
    ).run()
}
