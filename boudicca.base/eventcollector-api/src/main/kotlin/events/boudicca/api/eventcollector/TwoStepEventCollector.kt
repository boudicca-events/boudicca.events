package events.boudicca.api.eventcollector

import org.slf4j.LoggerFactory

abstract class TwoStepEventCollector<T>(private val name: String) : EventCollector {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    override fun getName(): String {
        return name
    }

    override fun collectEvents(): List<Event> {
        try {
            val allEvents: List<T>?
            try {
                allEvents = getAllUnparsedEvents()
            } catch (e: Exception) {
                LOG.error("collector ${getName()} throw exception while getting all unparsed events", e)
                return emptyList()
            }

            if (allEvents != null) {
                val mappedEvents = allEvents.flatMap {
                    var events: List<Event?> = listOf()
                    try {
                        val parsedEvents = parseMultipleEvents(it)
                        if (parsedEvents == null) {
                            LOG.error("collector ${getName()} returned null while parsing event: $it")
                        } else {
                            events = parsedEvents
                        }
                    } catch (e: Exception) {
                        LOG.error("collector ${getName()} throw exception while parsing event: $it", e)
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

    open fun parseMultipleEvents(event: T): List<Event?>? { //can be used by java so make nullable just to make sure
        return listOf(parseEvent(event))
    }

    open fun parseEvent(event: T): Event? { //can be used by java so make nullable just to make sure
        throw NotImplementedError("child classes has to either implement parseMultipleEvents or parseEvent")
    }

    abstract fun getAllUnparsedEvents(): List<T>? //can be used by java so make nullable just to make sure


    /**
     * will be called after all events are parsed so you can clean up caches and whatnot
     */
    open fun cleanup() {

    }
}