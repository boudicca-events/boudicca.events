package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.webui.WebuiApplication
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext

class EventCollectorWebUi(
    private val port: Int,
    private val eventCollectors: List<EventCollector>,
    private val otel: OpenTelemetry
) {

    private val logger = KotlinLogging.logger {}
    private var webuiApplication: ConfigurableApplicationContext? = null

    fun start() {
        logger.info { "webui starting and listening on $port" }
        webuiApplication = runApplication<WebuiApplication>("server.port=${port}") {
            this.addInitializers({
                it.beanFactory.registerSingleton("eventCollectors", eventCollectors)
                it.beanFactory.registerSingleton("otel", otel)
            })
        }
    }

    fun stop() {
        webuiApplication?.stop()
        webuiApplication = null
    }

}
