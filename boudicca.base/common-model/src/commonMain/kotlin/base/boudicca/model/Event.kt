package base.boudicca.model

import base.boudicca.format.Date
import base.boudicca.model.structured.StructuredEvent
import base.boudicca.model.structured.toEvent
import base.boudicca.model.structured.toFlatEntry

/**
 * a simple, unparsed, event. used mainly for serializing and sending/receiving it. for actually working with the values please consider transforming it into a [StructuredEvent]
 */
data class Event(
    val name: String,
    val startDate: Date,
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

        fun fromEntry(entry: Entry): Event? {
            return entry.toStructuredEntry().toEvent()?.toFlatEvent()
        }
    }
}
