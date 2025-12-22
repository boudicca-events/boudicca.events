package base.boudicca.api.eventcollector

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import java.time.Duration

class EventCollectorCoordinator(
    private val interval: Duration,
    private val eventCollectors: List<EventCollector>,
    private val eventCollectionRunner: EventCollectionRunner,
    private val otel: OpenTelemetry = GlobalOpenTelemetry.get(),
) : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private var eventCollectorWebUi: EventCollectorWebUi? = null

    fun startWebUi(port: Int = -1): EventCollectorCoordinator {
        val realPort =
            if (port == -1) {
                Configuration.getProperty("server.port")?.toInt()
                    ?: error { "you need to specify the server.port property!" }
            } else {
                port
            }
        synchronized(this) {
            if (eventCollectorWebUi == null) {
                eventCollectorWebUi = EventCollectorWebUi(realPort, eventCollectors, otel)
                eventCollectorWebUi!!.start()
            }
        }

        return this
    }

    fun run(): Nothing {
        while (true) {
            eventCollectionRunner.run()
            logger.info { "sleeping for $interval" }
            Thread.sleep(interval.toMillis())
        }
    }

    fun getCollectors(): List<EventCollector> = eventCollectors

    fun getEventCollectionRunner(): EventCollectionRunner = eventCollectionRunner

    override fun close() {
        synchronized(this) {
            if (eventCollectorWebUi != null) {
                eventCollectorWebUi!!.stop()
            }
        }
    }
}
