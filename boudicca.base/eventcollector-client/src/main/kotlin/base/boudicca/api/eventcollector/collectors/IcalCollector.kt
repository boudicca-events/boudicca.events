package base.boudicca.api.eventcollector.collectors

import base.boudicca.api.eventcollector.EventCollector
import base.boudicca.api.eventcollector.collectors.util.IcalParser
import base.boudicca.model.Event
import base.boudicca.model.structured.StructuredEvent
import biweekly.component.VEvent
import java.util.*

/**
 * EventCollector implementation which will collect events from ical resources.
 * implementations need to overwrite the #getAllIcalResources method and return fetched .ics files from there
 * implementations also can overwrite the #mapVEventToEvent method to read additional properties of the VEvent
 * implementations also can overwrite the #postProcess method to add custom properties or similar to the parsed events
 */
abstract class IcalCollector(private val name: String) : EventCollector {

    override fun getName(): String {
        return name
    }

    override fun collectStructuredEvents(): List<StructuredEvent> {
        val icalResources = getAllIcalResources()

        return icalResources
            .flatMap { parseSingleIcalResource(it) }
            .map { mapVEventToEvent(it) }
            .filter { it.isPresent }
            .map { postProcess(it.get()) }
    }

    /**
     * method which should return all ical resources (.ics files) as strings.
     */
    abstract fun getAllIcalResources(): List<String>

    /**
     * maps one VEvent to an (optional) Event. implementations can override this method to for example extract additional properties from the VEvent
     */
    open fun mapVEventToEvent(vEvent: VEvent): Optional<StructuredEvent> {
        return IcalParser.mapVEventToEvent(vEvent)
    }

    /**
     * postProcess the Event. can be overridden to add for example static additional properties to the Event.
     */
    open fun postProcess(event: StructuredEvent): StructuredEvent {
        return event
    }

    private fun parseSingleIcalResource(icalResource: String): List<VEvent> {
        return IcalParser.parseToVEvents(icalResource)
    }
}