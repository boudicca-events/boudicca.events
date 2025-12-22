package base.boudicca.model.structured

import base.boudicca.Property
import base.boudicca.SemanticKeys
import base.boudicca.format.DateFormatAdapter
import base.boudicca.keyfilters.KeyFilters
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.Event
import base.boudicca.model.structured.dsl.StructuredEventBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.OffsetDateTime
import java.util.*

/**
 * represents a parsed event, in the sense that all its keys have been parsed, and it has a lot of methods for filtering/selecting keys
 */
data class StructuredEvent(
    val name: String,
    val startDate: OffsetDateTime,
    val data: Map<Key, String> = emptyMap(),
) {
    private val logger = KotlinLogging.logger {}

    constructor(event: Event) : this(event.name, event.startDate, KeyUtils.toStructuredKeyValuePairs(event.data))

    fun toFlatEvent(): Event = Event(name, startDate, KeyUtils.toFlatKeyValuePairs(data))

    fun toEntry(): StructuredEntry = Companion.toEntry(this)

    fun toBuilder(): StructuredEventBuilder = StructuredEventBuilder(this.name, this.startDate, this.data)

    /**
     * get property values from this entry. please note that if a property value cannot be parsed it will silently ignore this value
     */
    fun <T> getProperty(property: Property<T>): List<Pair<Key, T>> = getProperty(property, null)

    /**
     * get property values with a specific language from this entry. please note that if a property value cannot be parsed it will silently ignore this value
     */
    fun <T> getProperty(
        property: Property<T>,
        language: String?,
    ): List<Pair<Key, T>> =
        KeyFilters
            .filterKeys(property.getKeyFilter(language), this)
            .mapNotNull {
                try {
                    val parsedValue = property.parseFromString(it.second)
                    Pair(it.first, parsedValue)
                } catch (e: IllegalArgumentException) {
                    logger.warn(e) { "error parsing value for key '${it.first}': ${it.second}" }
                    null
                }
            }

    fun filterKeys(keyFilter: KeyFilter): List<Pair<Key, String>> = KeyFilters.filterKeys(keyFilter, this)

    fun selectKey(keySelector: KeySelector): Optional<Pair<Key, String>> = keySelector.selectSingle(this)

    companion object {
        fun toEntry(event: StructuredEvent): StructuredEntry {
            val entry = event.data.toMutableMap()
            entry[Key.builder(SemanticKeys.NAME).build()] = event.name
            entry[
                Key
                    .builder(SemanticKeys.STARTDATE)
                    .withVariant(
                        VariantConstants.FORMAT_VARIANT_NAME,
                        VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME,
                    ).build(),
            ] =
                DateFormatAdapter().convertToString(event.startDate)
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
                            // we fall back to text here mainly for backwards compatibility
                            VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME,
                        ),
                    ).build()
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
            val startDate =
                try {
                    DateFormatAdapter().fromString(startDatePair.get().second)
                } catch (_: IllegalArgumentException) {
                    return Optional.empty()
                }
            val data = entry.toMutableMap()
            data.remove(namePair.get().first)
            data.remove(startDatePair.get().first)
            return Optional.of(StructuredEvent(name, startDate, data))
        }
    }
}
