package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.collectors.BoudiccaCollector
import base.boudicca.api.eventcollector.configuration.EventCollectorsConfigurationProperties
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface
import base.boudicca.api.eventcollector.runner.buildRunnerFor
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile

@Profile("!debug")
@SpringBootApplication
class LocalFetchFromOnlineBoudiccaApp(
    val configuration: EventCollectorsConfigurationProperties,
) : CommandLineRunner {
    override fun run(vararg args: String) {
        CollectionsFilter.alsoLog = true
        buildRunnerFor(listOf(BoudiccaCollector("https://eventdb.boudicca.events")))
            .withIngestion(RunnerIngestionInterface.createFromConfiguration(eventDbUrl = configuration.eventdbUrl, ingestAuth = configuration.ingestAuth))
            .withEnricher(RunnerEnricherInterface.createFromConfiguration(enricherUrl = configuration.enricherUrl))
            .run()
    }
}

fun main() {
    runApplication<LocalFetchFromOnlineBoudiccaApp>()
}
