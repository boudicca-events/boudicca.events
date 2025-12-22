package base.boudicca.query

import base.boudicca.SemanticKeys
import base.boudicca.keyfilters.KeySelector
import base.boudicca.model.Entry
import base.boudicca.model.structured.VariantConstants
import base.boudicca.model.structured.selectKey
import base.boudicca.model.toStructuredEntry
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

object Utils {
    private val logger = KotlinLogging.logger {}

    private const val DEFAULT_SIZE = 30

    fun offset(events: List<Entry>, offset: Int?, size: Int?): List<Entry> = events.drop(offset ?: 0).take(size ?: DEFAULT_SIZE)

    fun order(entries: Collection<Entry>, dateCache: ConcurrentHashMap<String, OffsetDateTime>): List<Entry> = entries.toList()
        .map { Pair(it, getStartDate(it, dateCache)) }
        .sortedWith(
            Comparator
                .comparing<Pair<Entry, OffsetDateTime>, OffsetDateTime> { it.second }
                .thenComparing(Function { it.first[SemanticKeys.NAME] ?: "" }),
        )
        .map { it.first }

    private fun getStartDate(entry: Entry, startDateCache: ConcurrentHashMap<String, OffsetDateTime>): OffsetDateTime {
        val optionalDateText =
            entry.toStructuredEntry().selectKey(
                KeySelector.builder(SemanticKeys.STARTDATE).thenVariant(
                    VariantConstants.FORMAT_VARIANT_NAME,
                    listOf(
                        VariantConstants.FormatVariantConstants.DATE_FORMAT_NAME,
                        VariantConstants.FormatVariantConstants.TEXT_FORMAT_NAME,
                    ),
                ).build(),
            )
        if (optionalDateText.isEmpty) {
            return Instant.ofEpochMilli(0).atOffset(ZoneOffset.MIN)
        }
        val dateText = optionalDateText.get().second
        if (startDateCache.containsKey(dateText)) {
            return startDateCache[dateText]!!
        }
        return try {
            val offsetDateTime =
                OffsetDateTime.parse(dateText, DateTimeFormatter.ISO_DATE_TIME)
                    .atZoneSameInstant(ZoneId.of("Europe/Vienna"))
                    .toOffsetDateTime()
            startDateCache[dateText] = offsetDateTime
            offsetDateTime
        } catch (e: DateTimeParseException) {
            logger.warn(e) { "error parsing date '$dateText'" }
            Instant.ofEpochMilli(0).atOffset(ZoneOffset.MIN)
        }
    }
}
