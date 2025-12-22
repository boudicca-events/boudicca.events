package base.boudicca.model

import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.toEvent
import base.boudicca.model.structured.toFlatEntry
import java.time.OffsetDateTime
import java.util.*

/**
 * a simple, unparsed, event. used mainly for serializing and sending/receiving it. for actually working with the values please consider transforming it into a [StructuredEvent]
 */
data class Event(val name: String, val startDate: OffsetDateTime, val data: Map<String, String> = mapOf()) {
    fun toStructuredEvent(): StructuredEvent = StructuredEvent(this)

    fun toEntry(): Entry = toEntry(this)

    companion object {
        fun toEntry(event: Event): Entry = event.toStructuredEvent().toEntry().toFlatEntry()

        fun fromEntry(entry: Entry): Optional<Event> = entry.toStructuredEntry().toEvent().map { it.toFlatEvent() }
    }
}
