package base.boudicca.model.structured

import base.boudicca.Property
import base.boudicca.SemanticKeys
import base.boudicca.format.DateFormat
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.Event
import java.time.OffsetDateTime
import java.util.*

/**
 * represents a parsed event, in the sense that all its keys have been parsed, and it has a lot of methods for filtering/selecting keys
 */
data class StructuredEvent(val name: String, val startDate: OffsetDateTime, val data: Map<Key, String> = emptyMap()) {
    constructor(event: Event) : this(event.name, event.startDate, KeyUtils.toStructuredKeyValuePairs(event.data))

    fun toFlatEvent(): Event {
        return Event(name, startDate, KeyUtils.toFlatKeyValuePairs(data))
    }

    fun toEntry(): StructuredEntry {
        return Companion.toEntry(this)
    }

    fun toBuilder(): StructuredEventBuilder {
        return StructuredEventBuilder(this.name, this.startDate, this.data)
    }

    /**
     * get property values from this entry. please note that if a property value cannot be parsed it will silently ignore this value
     */
    fun <T> getProperty(property: Property<T>): List<Pair<Key, T>> {
        return getProperty(property, null)
    }

    /**
     * get property values with a specific language from this entry. please note that if a property value cannot be parsed it will silently ignore this value
     */
    fun <T> getProperty(property: Property<T>, language: String?): List<Pair<Key, T>> {
        return KeyFilters
            .filterKeys(property.getKey(language), this)
            .mapNotNull {
                try {
                    val parsedValue = property.parseFromString(it.second)
                    Pair(it.first, parsedValue)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
    }

    fun filterKeys(keyFilter: Key): List<Pair<Key, String>> {
        return KeyFilters.filterKeys(keyFilter, this)
    }

    fun selectKey(keySelector: KeySelector): Optional<Pair<Key, String>> {
        return keySelector.selectSingle(this)
    }

    companion object {
        fun toEntry(event: StructuredEvent): StructuredEntry {
            val entry = event.data.toMutableMap()
            entry[Key.builder(SemanticKeys.NAME).build()] = event.name
            entry[Key.builder(SemanticKeys.STARTDATE).withVariant(
                VariantConstants.FORMAT_VARIANT_NAME,
                VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME
            ).build()] =
                DateFormat.parseToString(event.startDate)
            return entry
        }

        fun fromEntry(entry: StructuredEntry): Optional<StructuredEvent> {
            val startDatePair =
                KeySelector
                    .builder(SemanticKeys.STARTDATE)
                    .thenVariant(
                        VariantConstants.FORMAT_VARIANT_NAME,
                        listOf(
                            VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME,
                            //we fall back to text here mainly for backwards compatibility
                            VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME
                        )
                    )
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
            } catch (_: IllegalArgumentException) {
                return Optional.empty()
            }
            val data = entry.toMutableMap()
            data.remove(namePair.get().first)
            data.remove(startDatePair.get().first)
            return Optional.of(StructuredEvent(name, startDate, data))
        }

        fun builder(): StructuredEventBuilder {
            return StructuredEventBuilder(null, null)
        }

        fun builder(name: String, startDate: OffsetDateTime): StructuredEventBuilder {
            return StructuredEventBuilder(name, startDate)
        }
    }

    class StructuredEventBuilder internal constructor(
        private var name: String?,
        private var startDate: OffsetDateTime?,
        data: Map<Key, String> = emptyMap()
    ) : AbstractStructuredBuilder<StructuredEvent, StructuredEventBuilder>(data.toMutableMap()) {

        fun withName(name: String): StructuredEventBuilder {
            this.name = name
            return this
        }

        fun withStartDate(startDate: OffsetDateTime): StructuredEventBuilder {
            this.startDate = startDate
            return this
        }

        fun copy(): StructuredEventBuilder {
            return StructuredEventBuilder(name, startDate, data.toMutableMap())
        }

        override fun build(): StructuredEvent {
            return StructuredEvent(
                checkNotNull(name) { "name cannot be null for an event!" },
                checkNotNull(startDate) { "startDate cannot be null for an event!" },
                data.toMap()
            )
        }
    }
}
