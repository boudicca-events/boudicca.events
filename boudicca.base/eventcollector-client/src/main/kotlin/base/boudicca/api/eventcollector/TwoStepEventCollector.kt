package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.config.EventCollectorBaseConfig
import base.boudicca.model.Event
import base.boudicca.model.structured.StructuredEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

abstract class TwoStepEventCollector<T, U : EventCollectorBaseConfig>(
    // unfortunately this KClass is needed as parameter, because otherwise the type information is lost at runtime
    override val configClass: KClass<U>,
) : EventCollector<U>(configClass) {
    private val logger = KotlinLogging.logger {}

    override fun collectEvents(): List<Event> {
        try {
            val allEvents: List<T>?
            try {
                allEvents = getAllUnparsedEvents()
            } catch (e: Exception) {
                logger.error(e) { "collector ${getName()} throw exception while getting all unparsed events" }
                return emptyList()
            }

            if (allEvents != null) {
                val mappedEvents =
                    allEvents.flatMap {
                        var events: List<Event?> = listOf()
                        try {
                            val parsedEvents = parseMultipleEvents(it)
                            if (parsedEvents == null) {
                                logger.error { "collector ${getName()} returned null while parsing event: $it" }
                            } else {
                                events = parsedEvents
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "collector ${getName()} throw exception while parsing event: $it" }
                        }
                        events.filterNotNull()
                    }

                return mappedEvents
            }
            return emptyList()
        } finally {
            cleanup()
        }
    }

    open fun parseMultipleEvents(event: T): List<Event?>? { // can be used by java so make nullable just to make sure
        return parseMultipleStructuredEvents(event)?.map { it?.toFlatEvent() }
    }

    open fun parseEvent(event: T): Event? { // can be used by java so make nullable just to make sure
        throw NotImplementedError(
            "child classes have to either implement parseEvent, parseMultipleEvents," +
                " parseStructuredEvent or parseMultipleStructuredEvents",
        )
    }

    open fun parseMultipleStructuredEvents(event: T): List<StructuredEvent?>? { // can be used by java so make nullable just to make sure
        return listOf(parseStructuredEvent(event))
    }

    open fun parseStructuredEvent(event: T): StructuredEvent? { // can be used by java so make nullable just to make sure
        return parseEvent(event)?.toStructuredEvent()
    }

    abstract fun getAllUnparsedEvents(): List<T>? // can be used by java so make nullable just to make sure

    /**
     * will be called after all events are parsed, so you can clean up caches and whatnot
     */
    open fun cleanup() {
        // default nothing
    }
}
