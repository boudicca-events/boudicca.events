package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.runner.RunnerEnricherInterface
import base.boudicca.api.eventcollector.runner.RunnerIngestionInterface
import java.time.Duration

class EventCollectorCoordinatorBuilder {

    private val eventCollectors: MutableList<EventCollector> = mutableListOf()
    private var interval: Duration = Duration.ofHours(24)
    private var runnerIngestionInterface: RunnerIngestionInterface? = null
    private var runnerEnricherInterface: RunnerEnricherInterface? = null

    fun addEventCollector(eventCollector: EventCollector): EventCollectorCoordinatorBuilder {
        eventCollectors.add(eventCollector)
        return this
    }

    fun addEventCollectors(eventCollectors: Collection<EventCollector>): EventCollectorCoordinatorBuilder {
        this.eventCollectors.addAll(eventCollectors)
        return this
    }

    fun setCollectionInterval(interval: Duration): EventCollectorCoordinatorBuilder {
        this.interval = interval
        return this
    }

    fun setRunnerIngestionInterface(runnerIngestionInterface: RunnerIngestionInterface): EventCollectorCoordinatorBuilder {
        this.runnerIngestionInterface = runnerIngestionInterface
        return this
    }

    fun setRunnerEnricherInterface(runnerEnricherInterface: RunnerEnricherInterface): EventCollectorCoordinatorBuilder {
        this.runnerEnricherInterface = runnerEnricherInterface
        return this
    }

    fun build(): EventCollectorCoordinator {
        val finalEventCollectors = eventCollectors.toList() // make a copy to make it basically immutable
        return EventCollectorCoordinator(
            interval,
            finalEventCollectors,
            EventCollectionRunner(
                finalEventCollectors,
                runnerIngestionInterface ?: RunnerIngestionInterface.createFromConfiguration(),
                runnerEnricherInterface ?: RunnerEnricherInterface.createFromConfiguration()
            )
        )
    }
}
