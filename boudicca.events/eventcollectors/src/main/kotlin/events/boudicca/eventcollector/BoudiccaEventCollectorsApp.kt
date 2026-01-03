package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectionRunner
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.configuration.EventCollectorsConfigurationProperties
import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface
import base.boudicca.springboot.common.MonitoringConfigurationProperties
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Profile("!debug")
@SpringBootApplication
@EnableScheduling
class BoudiccaEventCollectorsApp(
    private val eventCollectors: List<EventCollector>,
    private val eventCollectorsConfigurationProperties: EventCollectorsConfigurationProperties,
    private val monitoringConfigurationProperties: MonitoringConfigurationProperties,
    private val otel: OpenTelemetry,
) {
    val ingestionInterface =
        RunnerIngestionInterface.createFromConfiguration(
            eventDbUrl = eventCollectorsConfigurationProperties.eventdbUrl,
            ingestAuth = eventCollectorsConfigurationProperties.ingestAuth,
        )
    val enricherInterface =
        RunnerEnricherInterface.createFromConfiguration(
            enricherUrl = eventCollectorsConfigurationProperties.enricherUrl,
        )

    private val eventCollectionRunner = initRunner()

    fun initRunner(): EventCollectionRunner {
        GlobalOpenTelemetry.set(otel)
        return EventCollectionRunner(otel)
    }

    @Bean
    fun eventCollectionRunner(): EventCollectionRunner = eventCollectionRunner

    @Scheduled(fixedRateString = "\${boudicca.collector.collection-interval}")
    private fun runCollection() {
        this.eventCollectionRunner.run(
            eventCollectors,
            ingestionInterface,
            enricherInterface,
        )
    }
}

fun main(args: Array<String>) {
    runApplication<BoudiccaEventCollectorsApp>(*args)
}
