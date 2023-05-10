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
            println("collector ${getName()} throw exception while getting all unparsed events")
            e.printStackTrace()
            return emptyList()
        }

        if (allEvents != null) {
            val mappedEvents = allEvents.mapNotNull {
                var event: Event? = null
                try {
                    event = parseEvent(it)
                    if (event == null) {
                        println("collector ${getName()} returned null while parsing event: $it")
                    }
                } catch (e: Exception) {
                    println("collector ${getName()} throw exception while parsing event: $it")
                    e.printStackTrace()
                }
                event
            }

            return mappedEvents
        }
        return emptyList()
    }

    abstract fun parseEvent(event: T): Event? //can be used by java so make nullable just to make sure

    abstract fun getAllUnparsedEvents(): List<T>? //can be used by java so make nullable just to make sure
}