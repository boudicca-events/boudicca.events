package base.boudicca.model.structured

import base.boudicca.SemanticKeys
import base.boudicca.format.DateFormat
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.Event
import java.time.OffsetDateTime
import java.util.*

data class StructuredEvent(val name: String, val startDate: OffsetDateTime, val data: Map<Key, String>) {
    constructor(event: Event) : this(event.name, event.startDate, KeyUtils.toStructuredKeyValuePairs(event.data))

    fun toFlatEvent(): Event {
        return Event(name, startDate, KeyUtils.toFlatKeyValuePairs(data))
    }

    fun toEntry():StructuredEntry {
        return Companion.toEntry(this)
    }

    companion object {
        fun toEntry(event: StructuredEvent): StructuredEntry {
            //TODO use entry / event builder here?
            val entry = event.data.toMutableMap()
            entry[Key.builder(SemanticKeys.NAME).build()] = event.name
            //TODO do not use those magic strings
            entry[Key.builder(SemanticKeys.STARTDATE ).withVariant("format","date").build()] = DateFormat.parseToString(event.startDate)
            return entry
        }

        fun fromEntry(entry: StructuredEntry): Optional<StructuredEvent> {
            //TODO use entry / event builder here?
            val startDatePair =
                KeySelector
                    .builder(SemanticKeys.STARTDATE)
                    .thenVariant(
                        "format",
                        listOf("date", "")
                    ) //we fall back to text here mainly for backwards compatibility
                    .build()
                    .selectSingle(entry)
            val namePair =
                KeySelector
                    .builder(SemanticKeys.NAME)
                    .build()
                    .selectSingle(entry)
            if (namePair.isEmpty || startDatePair.isEmpty) {
                return Optional.empty()
            }
            val name = namePair.get().second
            val startDate = try {
                DateFormat.parseFromString(startDatePair.get().second)
            } catch (e: IllegalArgumentException) {
                return Optional.empty()
            }
            val data = entry.toMutableMap()
            data.remove(namePair.get().first)
            data.remove(startDatePair.get().first)
            return Optional.of(StructuredEvent(name, startDate, data))
        }
    }
}