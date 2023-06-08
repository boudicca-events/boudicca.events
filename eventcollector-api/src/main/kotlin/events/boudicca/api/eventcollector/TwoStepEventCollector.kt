package events.boudicca.api.eventcollector

abstract class TwoStepEventCollector<T>(private val name: String) : EventCollector {
    override fun getName(): String {
        return name
    }

    override fun collectEvents(): List<Event> {
        val allEvents: List<T>?
        try {
            allEvents = getAllUnparsedEvents()
        } catch (e: Exception) {
            System.err.println("collector ${getName()} throw exception while getting all unparsed events")
            e.printStackTrace()
            return emptyList()
        }

        if (allEvents != null) {
            val mappedEvents = allEvents.flatMap {
                var events: List<Event?> = listOf()
                try {
                    val parsedEvents = parseMultipleEvents(it)
                    if (parsedEvents == null) {
                        System.err.println("collector ${getName()} returned null while parsing event: $it")
                    } else {
                        events = parsedEvents
                    }
                } catch (e: Exception) {
                    System.err.println("collector ${getName()} throw exception while parsing event: $it")
                    e.printStackTrace()
                }
                events.filterNotNull()
            }

            return mappedEvents
        }
        return emptyList()
    }

    open fun parseMultipleEvents(event: T): List<Event?>? { //can be used by java so make nullable just to make sure
        return listOf(parseEvent(event))
    }

    open fun parseEvent(event: T): Event? { //can be used by java so make nullable just to make sure
        throw NotImplementedError("child classes has to either implement parseMultipleEvents or parseEvent")
    }

    abstract fun getAllUnparsedEvents(): List<T>? //can be used by java so make nullable just to make sure
}