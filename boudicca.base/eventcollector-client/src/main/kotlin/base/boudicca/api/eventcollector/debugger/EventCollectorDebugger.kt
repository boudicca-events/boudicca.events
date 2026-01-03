package base.boudicca.api.eventcollector.debugger

import base.boudicca.SemanticKeys
import base.boudicca.api.eventcollector.collections.Collections
import base.boudicca.api.eventcollector.configuration.EventCollectorsConfigurationProperties
import base.boudicca.api.eventcollector.debugger.color.green
import base.boudicca.api.eventcollector.debugger.color.red
import base.boudicca.api.eventcollector.debugger.color.yellow
import base.boudicca.api.eventcollector.logging.CollectionsFilter
import base.boudicca.api.eventcollector.runner.DebugRunner
import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.model.Event

class EventCollectorDebugger(
    val runner: DebugRunner,
    val configuration: EventCollectorsConfigurationProperties,
    val verboseDebugging: Boolean = true,
    val verboseValidation: Boolean = true,
) {
    init {
        CollectionsFilter.alsoLog = true
        FetcherFactory.disableRetries = true
    }

    private val allEvents = mutableListOf<Event>()

    fun runDebug(): EventCollectorDebugger {
        val collectedEvents = runner.run()

        if (verboseDebugging) {
            collectedEvents.forEach {
                println(it)
            }
        }
        allEvents.addAll(collectedEvents)
        val fullCollection = Collections.getLastFullCollection()
        println(fullCollection)
        println("debugger collected ${collectedEvents.size} events")
        val errorCount = fullCollection.getTotalErrorCount()
        if (errorCount != 0) {
            println("found $errorCount errors!")
        }
        val warningCount = fullCollection.getTotalWarningCount()
        if (warningCount != 0) {
            println("found $warningCount warnings!")
        }

        if (configuration.webui.enabled) {
            println("webui is running on http://localhost:${configuration.webui.port} and is blocking until you press enter in this console")
            readlnOrNull()
        }

        return this
    }

    fun validate(validations: List<EventCollectorValidation>): EventCollectorDebugger {
        allEvents.forEach { event ->
            println("=========================================================================")
            println("${event.data[SemanticKeys.COLLECTORNAME]} - ${event.startDate} ${event.name}")
            val highestSeverity =
                validations.minOfOrNull { validation ->
                    validation.validate(event, verboseValidation)
                }
            if (highestSeverity == ValidationResult.Error) {
                println("Validation: CHECK FAILED".red())
            } else if (highestSeverity == ValidationResult.Warn) {
                println("Validation: CHECK WARN".yellow())
            } else {
                println("Validation: CHECK OK".green())
            }
            println()
            println()
        }
        return this
    }

    fun clearEvents(): EventCollectorDebugger {
        allEvents.clear()
        return this
    }
}
