package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.EventCollectionRunner
import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.annotations.BoudiccaEventCollector
import base.boudicca.api.eventcollector.configuration.EventCollectorsConfigurationProperties
import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface
import base.boudicca.springboot.common.MonitoringConfigurationProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import org.springframework.beans.factory.findAnnotationOnBean
import org.springframework.beans.factory.getBeanNamesForAnnotation
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Profile("!debug")
@SpringBootApplication
@EnableScheduling
class BoudiccaEventCollectorsApp(
    private val applicationContext: ApplicationContext,
    private val eventCollectorsConfigurationProperties: EventCollectorsConfigurationProperties,
    private val monitoringConfigurationProperties: MonitoringConfigurationProperties,
    private val otel: OpenTelemetry,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

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

    private val enabledCollectors: List<EventCollector<*>> = initCollectors()

    private fun initCollectors(): List<EventCollector<*>> {
        // This is a bit of spring black magic, but it allows us to configure eventcollectors at runtime
        // and run multiple collectors of the same type at once.
        // First we find all registered collector beans.
        val typeToBeanName =
            applicationContext
                .getBeanNamesForAnnotation<BoudiccaEventCollector>()
                .associateBy { beanName ->
                    applicationContext.findAnnotationOnBean<BoudiccaEventCollector>(beanName)?.value
                        ?: beanName
                }

        // now we iterate over all the configurations
        return eventCollectorsConfigurationProperties.collectors
            .filter { it.enabled }
            .mapNotNull { config ->
                // get the correct bean for the type
                val beanName = typeToBeanName[config.type]
                if (beanName == null) {
                    logger.warn { "Collector of type ${config.type} not found" }
                    null
                } else {
                    // create a new instance of the collector with the given configuration
                    val collector = applicationContext.getBean(beanName) as EventCollector<*>
                    collector.configure(config.name, config.properties)
                    collector
                }
            }
    }

    fun initRunner(): EventCollectionRunner {
        GlobalOpenTelemetry.set(otel)
        return EventCollectionRunner(otel)
    }

    @Bean
    fun eventCollectionRunner(): EventCollectionRunner = eventCollectionRunner

    @Scheduled(fixedRateString = "\${boudicca.collector.collection-interval}")
    private fun runCollection() {
        this.eventCollectionRunner.run(
            enabledCollectors,
            ingestionInterface,
            enricherInterface,
        )
    }
}

fun main(args: Array<String>) {
    SpringApplicationBuilder(BoudiccaEventCollectorsApp::class.java)
        .web(WebApplicationType.SERVLET)
        .build()
        .run(*args)
}
