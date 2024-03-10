package base.boudicca.api.eventcollector

import base.boudicca.model.Event
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Function

@Deprecated("use EventCollectorCoordinator + EventCollectorCoordinatorBuilder instead")
class EventCollectorScheduler(
    private val interval: Duration = Duration.ofHours(24),
    private val eventSink: Consumer<List<Event>>? = null,
    private val enricherFunction: Function<List<Event>, List<Event>>? = null
) : AutoCloseable {

    private val builder = EventCollectorCoordinatorBuilder()
    private var eventCollectorCoordinator: EventCollectorCoordinator? = null
    private var shouldStartWebUi = false
    private var webUiPort = -1

    fun addEventCollector(eventCollector: EventCollector): EventCollectorScheduler {
        builder.addEventCollector(eventCollector)
        return this
    }

    fun startWebUi(port: Int = -1): EventCollectorScheduler {
        shouldStartWebUi = true
        webUiPort = port
        return this
    }

    private fun getEventCollectorCoordinator(): EventCollectorCoordinator {
        if (eventCollectorCoordinator == null) {
            builder.setCollectionInterval(interval)
            if (eventSink != null) {
                builder.setEventSink(eventSink)
            }
            if (enricherFunction != null) {
                builder.setEnricherFunction(enricherFunction)
            }
            eventCollectorCoordinator = builder.build()
            if (shouldStartWebUi) {
                eventCollectorCoordinator!!.startWebUi(webUiPort)
            }
        }
        return eventCollectorCoordinator!!
    }

    fun run(): Nothing {
        getEventCollectorCoordinator().run()
    }

    fun runOnce() {
        getEventCollectorCoordinator().getEventCollectionRunner().run()
    }

    fun getCollectors(): List<EventCollector> {
        return getEventCollectorCoordinator().getCollectors()
    }

    override fun close() {
        getEventCollectorCoordinator().close()
    }

}