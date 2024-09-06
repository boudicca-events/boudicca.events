package base.boudicca.model

import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.toEvent
import base.boudicca.model.structured.toFlatEntry
import java.time.OffsetDateTime
import java.util.*

data class Event(
    val name: String,
    val startDate: OffsetDateTime,
    val data: Map<String, String> = mapOf()
) {

    fun toStructuredEvent(): StructuredEvent {
        return StructuredEvent(this)
    }

    fun toEntry(): Entry {
        return toEntry(this)
    }

    companion object {
        fun toEntry(event: Event): Entry {
            return event.toStructuredEvent().toEntry().toFlatEntry()
        }

        fun fromEntry(entry: Entry): Optional<Event> {
            return entry.toStructuredEntry().toEvent().map { it.toFlatEvent() }
        }
    }
}